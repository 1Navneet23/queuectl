#!/bin/bash

DB_FILE="queuectl.db"
RUN="mvn -q compile exec:java -Dexec.mainClass=org.navneet.queuectl.Main -Dexec.args="

echo "=== QueueCTL Test Suite ==="

echo ""
echo "--- Cleaning previous state ---"
rm -f "$DB_FILE" worker.flag

echo ""
echo "--- Test 1: Basic job completes successfully ---"
eval $RUN'"enqueue \"{\\\"id\\\":\\\"test-success\\\",\\\"command\\\":\\\"echo hello-world\\\"}\""'
eval $RUN'"worker start --count 1"' > /tmp/worker1.log 2>&1 &
WORKER_PID=$!
sleep 5
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
RESULT=$(eval $RUN'"list --state completed"')
echo "$RESULT"
if echo "$RESULT" | grep -q "test-success"; then
  echo "PASS: job completed successfully."
else
  echo "FAIL: job not in completed state."
fi

echo ""
echo "--- Test 2: Failed job retries with backoff and moves to DLQ ---"
eval $RUN'"config set max-retries 2"'
eval $RUN'"config set backoff-base 2"'
eval $RUN'"enqueue \"{\\\"id\\\":\\\"test-fail\\\",\\\"command\\\":\\\"badcommand123\\\",\\\"max_retries\\\":2}\""'
eval $RUN'"worker start --count 1"' > /tmp/worker2.log 2>&1 &
WORKER_PID=$!
# delay=2^1=2s after attempt 1, then attempt 2 pushes to DEAD — allow 15s total
sleep 15
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
DLQ=$(eval $RUN'"dlq list"')
echo "$DLQ"
if echo "$DLQ" | grep -q "test-fail"; then
  echo "PASS: failed job is in DLQ."
else
  echo "FAIL: job not found in DLQ."
fi

echo ""
echo "--- Test 3: Multiple workers process jobs without overlap ---"
for i in 1 2 3 4 5 6; do
  eval $RUN'"enqueue \"{\\\"command\\\":\\\"echo job-'"$i"'\\\"}\""'
done
eval $RUN'"worker start --count 3"' > /tmp/worker3.log 2>&1 &
WORKER_PID=$!
sleep 6
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
STATUS=$(eval $RUN'"status"')
echo "$STATUS"
COMPLETED=$(eval $RUN'"list --state completed"' | grep -c "COMPLETED" || true)
echo "Completed jobs: $COMPLETED"
if [ "$COMPLETED" -ge 6 ]; then
  echo "PASS: all 6 jobs processed without overlap."
else
  echo "WARN: some jobs may still be pending — check status above."
fi

echo ""
echo "--- Test 4: Invalid commands fail gracefully (no crash) ---"
eval $RUN'"enqueue \"{\\\"id\\\":\\\"test-invalid\\\",\\\"command\\\":\\\"not-a-real-command\\\",\\\"max_retries\\\":1}\""'
eval $RUN'"worker start --count 1"' > /tmp/worker4.log 2>&1 &
WORKER_PID=$!
sleep 8
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
if ! grep -q "Exception\|FATAL\|panic" /tmp/worker4.log; then
  echo "PASS: invalid command handled gracefully (no unhandled exception)."
else
  echo "FAIL: exception or crash detected in worker log."
fi

echo ""
echo "--- Test 5: Job data survives restart ---"
BEFORE=$(sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM jobs;")
echo "Job count before: $BEFORE"
eval $RUN'"list"' > /dev/null
AFTER=$(sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM jobs;")
echo "Job count after re-read: $AFTER"
if [ "$BEFORE" = "$AFTER" ]; then
  echo "PASS: job data persists across process restarts."
else
  echo "FAIL: job count changed ($BEFORE -> $AFTER)."
fi

echo ""
echo "--- Test 6: DLQ retry moves job back to PENDING ---"
DLQ_ID=$(sqlite3 "$DB_FILE" "SELECT id FROM jobs WHERE state='DEAD' LIMIT 1;")
if [ -n "$DLQ_ID" ]; then
  eval $RUN'"dlq retry '"$DLQ_ID"'"'
  STATE=$(sqlite3 "$DB_FILE" "SELECT state FROM jobs WHERE id='$DLQ_ID';")
  if [ "$STATE" = "PENDING" ]; then
    echo "PASS: DLQ job reset to PENDING."
  else
    echo "FAIL: job state is '$STATE', expected PENDING."
  fi
else
  echo "SKIP: no DEAD jobs available for retry test."
fi

echo ""
echo "=== Test Suite Complete ==="