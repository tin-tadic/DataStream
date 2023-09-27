# Data Stream

Data Stream is a SpringBoot app using mongo changestreams to access real-time data changes

It consists of two submodules with different goals, acting as a pub-sub pair:

- **Streamer:**
    1. Reads the change stream and creates third party API messages
    2. Saves them into its own db (`dataStreamApplication.data`)
    3. Periodically sends them to rmq for the Consumer

- **Consumer:**
    1. Polls the rmq queue (polling consumer, not a queue listener)
    2. Sends the tax transactions to third party API

## Minimum requirements
Java: JDK 11+  
Docker and docker-compose

## Queue Messages Batch Job Info
Queries database for unpublished messages and sends them to RMQ for processing  
It runs constantly via @Scheduler annotation.

## Running locally
Application can be started with `docker-compose up -d`, then running instances of Streamer and Consumer.  
As it runs locally from the `application-dev.properties` file, `SPRING_PROFILES_ACTIVE` must be set to `dev`.

## Common issues

```
"errmsg": "Resume of change stream was not possible, as the resume point may no longer be in the oplog."
```
The stream is running so late it cannot recover.  
To generate a new token, go to `dataStreamApplication` database on the affected cluster and delete the entry in the `resume-token` collection.

#### Scheduled jobs/tasks are not being executed
This will fix itself after up to 10 minutes, but if you want them to execute immediately (useful for testing), then just delete the entry from `dataStreamApplication.shedLock`
