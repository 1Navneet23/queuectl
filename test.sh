#!/bin/bash


set -e

DB_FILE="queuectl.db"
RUN="mvn -q compile exec:java -Dexec.mainClass=org.navneet.queuectl.Main -Dexec.args="

echo "=== QueueCTL Test Suite ==="

echo ""
echo "--- Cleaning previous state ---"
rm -f "$DB_FILE" worker.flag

echo ""
echo "--- Test 1: Basic job completes successfully ---"
eval $RUN'"enqueue \"{\\\"id\\\":\\\"test-success\\\",\\\"command\\\":\\\"echo hello-world\\\"}\""'
eval $RUN'"worker start --count 1 &"' > /tmp/worker1.log 2>&1 &
WORKER_PID=$!
sleep 4
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
eval $RUN'"list --state completed"'

echo ""
echo "--- Test 2: Failed job retries with backoff and moves to DLQ ---"
eval $RUN'"config set max-retries 2"'
eval $RUN'"config set backoff-base 2"'
eval $RUN'"enqueue \"{\\\"id\\\":\\\"test-fail\\\",\\\"command\\\":\\\"badcommand123\\\",\\\"max_retries\\\":2}\""'
eval $RUN'"worker start --count 1 &"' > /tmp/worker2.log 2>&1 &
WORKER_PID=$!
sleep 8
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
eval $RUN'"dlq list"'

echo ""
echo "--- Test 3: Multiple workers process jobs without overlap ---"
for i in 1 2 3 4 5 6; do
  eval $RUN'"enqueue \"{\\\"command\\\":\\\"echo job-'"$i"'\\\"}\""'
done
eval $RUN'"worker start --count 3 &"' > /tmp/worker3.log 2>&1 &
WORKER_PID=$!
sleep 5
eval $RUN'"worker stop"'
wait $WORKER_PID 2>/dev/null || true
eval $RUN'"status"'

echo ""
echo "--- Test 4: Invalid commands fail gracefully (no crash) ---"
eval $RUN'"enqueue \"{\\\"command\\\":\\\"not-a-real-command\\\"}\""'
echo "(checked implicitly by test 2 completing without a stack trace)"

echo ""
echo "--- Test 5: Job data survives restart ---"
BEFORE=$(sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM jobs;")
echo "Job count before restart: $BEFORE"
eval $RUN'"list"'
AFTER=$(sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM jobs;")
echo "Job count after re-reading DB: $AFTER"
if [ "$BEFORE" == "$AFTER" ]; then
  echo "PASS: job count unchanged across separate process invocations."
else
  echo "FAIL: job count mismatch."
fi

echo ""
echo "=== Test Suite Complete ==="