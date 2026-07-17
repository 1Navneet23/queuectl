# QueueCTL

A Java-based command-line background job queue system with persistent
SQLite storage, concurrent worker execution, configurable retries with
exponential backoff, crash recovery, and a Dead Letter Queue (DLQ).

> This project was developed as part of the **Backend Developer
> Internship Assignment**. It implements all required features from the
> assignment along with several bonus features such as job timeout
> handling, execution logging, metrics, and automatic recovery of stale
> jobs.

------------------------------------------------------------------------

## 🎥 Demo Video

[▶ Watch the QueueCTL Demo Video](https://drive.google.com/file/d/19NhIzGZ8lFjdo6yr54EImlKPwOsRxzWG/view?usp=sharing)

------------------------------------------------------------------------

# Features

## Core Features

-   Persistent job storage using SQLite
-   Concurrent worker execution
-   Graceful worker shutdown
-   Retry mechanism with exponential backoff
-   Dead Letter Queue (DLQ)
-   Retry jobs from the DLQ
-   Runtime configuration management
-   Clean CLI interface using Picocli

## Bonus Features

-   Job timeout configuration
-   Automatic recovery of stale `PROCESSING` jobs
-   Job execution logging
-   Queue metrics and execution statistics

------------------------------------------------------------------------

# Tech Stack

-   Java 22
-   Maven
-   Picocli
-   SQLite (`sqlite-jdbc`)
-   Jackson

------------------------------------------------------------------------

# Setup

## Prerequisites

-   JDK 22+
-   Maven 3.8+

## Clone

``` bash
git clone https://github.com/<your-username>/queuectl.git
cd queuectl
```

## Build

``` bash
mvn clean package
```

## Run

``` bash
java -jar target/queuectl-1.0-SNAPSHOT.jar
```

or

``` bash
mvn exec:java -Dexec.mainClass="org.navneet.queuectl.Main"
```

The application automatically creates:

    queuectl.db
    logs/
    worker.flag

------------------------------------------------------------------------

# Usage

## Enqueue

``` bash
queuectl enqueue '{"command":"echo Hello World"}'
```

or

``` bash
queuectl enqueue '{"id":"job1","command":"echo Hello World"}'
```

## Start Workers

``` bash
queuectl worker start --count 3
```

Stop workers gracefully:

``` bash
queuectl worker stop
```

## List Jobs

``` bash
queuectl list
queuectl list --state pending
queuectl list --state processing
queuectl list --state completed
queuectl list --state failed
queuectl list --state dead
```

## Queue Status

``` bash
queuectl status
```

Displays:

-   Total Jobs
-   Pending Jobs
-   Processing Jobs
-   Completed Jobs
-   Failed Jobs
-   Dead Jobs
-   Active Workers

## Metrics

``` bash
queuectl metrics
```

Displays:

-   Total Jobs
-   Completed Jobs
-   Dead Jobs
-   Success Rate
-   Average Attempts
-   Average Execution Time
-   Timeout Statistics

## Dead Letter Queue

``` bash
queuectl dlq list
queuectl dlq retry <job-id>
```

## Configuration

``` bash
queuectl config get max-retries
queuectl config get backoff-base
queuectl config get job-timeout

queuectl config set max-retries 3
queuectl config set backoff-base 2
queuectl config set job-timeout 30
```

------------------------------------------------------------------------

# Architecture

                     QueueCTL CLI
                           │
                           ▼
                   Command Layer
                           │
                           ▼
                   Worker Manager
                           │
                  Multiple Workers
                           │
                           ▼
                 Command Executor
                           │
                           ▼
                   SQLite Database

## Job Lifecycle

    PENDING
       │
       ▼
    PROCESSING
       │
     ┌─┴─────────────┐
     ▼               ▼
    COMPLETED     FAILED
                     │
          attempts < maxRetries
                     │
                     ▼
          Exponential Backoff
                     │
                     ▼
                 PENDING
                     │
          attempts >= maxRetries
                     │
                     ▼
                    DEAD

## Persistence

Jobs and configuration are stored in SQLite.

    queuectl.db

SQLite WAL mode and busy timeout are enabled to support concurrent
worker execution safely.

## Worker Management

-   Multiple workers process jobs concurrently.
-   Atomic job claiming prevents duplicate execution.
-   Workers finish the current job before shutting down.
-   Stale `PROCESSING` jobs are automatically recovered on startup.

## Retry Strategy

    delay = backoffBase ^ attempts

Example (`backoff-base = 2`):

    Attempt       Delay
  --------- -----------
          1   2 seconds
          2   4 seconds
          3   8 seconds

After exhausting retries, the job is moved to the Dead Letter Queue.

## Logging

Every execution creates a log file in:

    logs/

Example:

    logs/
    ├── job1_attempt0.log
    ├── job1_attempt1.log
    └── job2_attempt0.log

## Metrics

The `metrics` command reports:

-   Total jobs
-   Completed jobs
-   Dead jobs
-   Success rate
-   Average attempts
-   Average execution time
-   Timeout statistics

------------------------------------------------------------------------

# Project Structure

    src
    ├── cli
    ├── database
    ├── executor
    ├── model
    ├── repository
    ├── worker
    └── Main.java

------------------------------------------------------------------------

# Assumptions & Trade-offs

-   Active workers shown by `status` are inferred from jobs currently in
    the `PROCESSING` state.
-   DLQ retry resets the attempt counter to zero, providing a fresh
    retry budget.
-   Commands execute using `cmd /c` on Windows and `sh -c` on Unix-like
    systems.

------------------------------------------------------------------------

# Testing

The project was manually verified against all required scenarios.

## 1. Basic Job Execution

``` bash
queuectl enqueue '{"command":"echo Hello"}'
queuectl worker start
```

Expected:

-   Job completes successfully.

## 2. Retry & Dead Letter Queue

``` bash
queuectl enqueue '{"command":"invalidcommand"}'
queuectl worker start
```

Expected:

-   Automatic retries
-   Exponential backoff
-   Job moves to DLQ after maximum retries

## 3. Multiple Workers

``` bash
queuectl worker start --count 3
```

Expected:

-   Concurrent processing
-   No duplicate execution

## 4. Invalid Command Handling

Expected:

-   Failure handled gracefully
-   Retry mechanism triggered
-   Execution logs generated

## 5. Persistence

-   Enqueue jobs
-   Stop QueueCTL
-   Restart QueueCTL

Expected:

-   Jobs remain in SQLite
-   Processing resumes correctly

A sample `test.sh` is included for Unix-like systems. Development and
verification were performed manually on Windows using the CLI commands
above.

------------------------------------------------------------------------

# Bonus Features Implemented

-   Job timeout handling
-   Execution logging
-   Queue metrics
-   Crash recovery
-   Runtime configuration
-   Graceful shutdown
-   DLQ retry

------------------------------------------------------------------------

# Future Improvements

-   Worker heartbeat monitoring
-   Job priority queues
-   Scheduled jobs (`run_at`)
-   REST API
-   Web dashboard

------------------------------------------------------------------------

# License

This project was developed as part of a Backend Developer Internship
Assignment.
