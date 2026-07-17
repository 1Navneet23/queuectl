# QueueCTL

A Java-based command-line background job queue system with persistent
SQLite storage, concurrent workers, configurable retries with
exponential backoff, crash recovery, and a Dead Letter Queue (DLQ).

> This project was developed as part of the Backend Developer Internship
> Assignment. It implements all required features from the assignment
> and several bonus features including job timeout support, execution
> logging, metrics, and crash recovery.

## Features

### Core Features

-   Persistent job storage using SQLite
-   Concurrent worker execution
-   Graceful worker shutdown
-   Retry mechanism with exponential backoff
-   Dead Letter Queue (DLQ)
-   Retry jobs from the DLQ
-   Runtime configuration management
-   Clean CLI interface using Picocli

### Bonus Features

-   Job timeout configuration
-   Automatic recovery of stale PROCESSING jobs
-   Job execution logging
-   Queue metrics and execution statistics

------------------------------------------------------------------------

## Tech Stack

-   Java 22
-   Maven
-   Picocli
-   SQLite (sqlite-jdbc)
-   Jackson

------------------------------------------------------------------------

## Setup

### Prerequisites

-   JDK 22+
-   Maven 3.8+

### Build

``` bash
mvn clean package
```

Run with:

``` bash
java -jar target/queuectl.jar
```

or

``` bash
mvn exec:java -Dexec.mainClass="org.navneet.queuectl.Main"
```

The application automatically creates:

-   `queuectl.db`
-   `logs/`
-   `worker.flag`

------------------------------------------------------------------------

## Usage

### Enqueue

``` bash
queuectl enqueue '{"command":"echo Hello World"}'
```

or

``` bash
queuectl enqueue '{"id":"job1","command":"echo Hello World"}'
```

### Start Workers

``` bash
queuectl worker start --count 3
```

Stop workers from another terminal:

``` bash
queuectl worker stop
```

### List Jobs

``` bash
queuectl list
queuectl list --state pending
queuectl list --state processing
queuectl list --state completed
queuectl list --state dead
```

### Queue Status

``` bash
queuectl status
```

### Metrics

``` bash
queuectl metrics
```

### Dead Letter Queue

``` bash
queuectl dlq list
queuectl dlq retry <job-id>
```

### Configuration

``` bash
queuectl config get max-retries
queuectl config get backoff-base
queuectl config get job-timeout

queuectl config set max-retries 3
queuectl config set backoff-base 2
queuectl config set job-timeout 30
```

------------------------------------------------------------------------

## Architecture

    QueueCTL CLI
          │
          ▼
    Command Layer
          │
          ▼
    Worker Manager
          │
          ▼
    Multiple Workers
          │
          ▼
    Command Executor
          │
          ▼
    SQLite Database

### Job Lifecycle

    PENDING
       │
       ▼
    PROCESSING
       │
     ┌─┴────────────┐
     ▼             ▼
    COMPLETED    FAILED
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

### Persistence

Jobs and configuration are stored in SQLite. Data survives application
restarts.

### Worker Management

-   Multiple workers execute jobs concurrently.
-   Atomic job claiming prevents duplicate execution.
-   Workers shut down gracefully after finishing the current job.
-   Stale PROCESSING jobs are automatically recovered when workers start
    again.

### Retry Strategy

    delay = backoffBase ^ attempts

### Logging

Every execution creates a log file inside:

    logs/

### Metrics

The metrics command reports:

-   Total jobs
-   Completed jobs
-   Dead jobs
-   Success rate
-   Average attempts
-   Average execution time
-   Timeout statistics

------------------------------------------------------------------------

## Assumptions & Trade-offs

-   Active workers are inferred from jobs currently in the PROCESSING
    state.
-   DLQ retry resets the attempt counter, giving the job a fresh retry
    budget.
-   Commands are executed using `cmd /c` on Windows and `sh -c` on
    Unix-like systems.

------------------------------------------------------------------------

## Testing

The project has been manually tested for:

-   Successful job execution
-   Failed job retries
-   Dead Letter Queue
-   Multi-worker processing
-   Crash recovery
-   Graceful shutdown
-   Configuration management
-   Metrics
-   Timeout handling
-   Persistent storage across restarts

A sample `test.sh` script is also included for Unix-like systems.

------------------------------------------------------------------------

 