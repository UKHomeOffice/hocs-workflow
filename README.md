# HOCS Workflow Service

[![CodeQL](https://github.com/UKHomeOffice/hocs-workflow/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/UKHomeOffice/hocs-workflow/actions/workflows/codeql-analysis.yml)

This is the Home Office Correspondence Service (HOCS) workflow service.

## Developing locally
 
The service will run against localstack using the `local` Spring profile and the docker-compose services in `docker/docker-compose.yml`
 
To start local instances run `./docker/docker-compose up`.

## Getting Started

### Prerequisites

* ```Java 11```
* ```Docker```
* ```AWS SQS queues``` (document and case output) or localstack
* ```Postgres``` 
 
## Build and Run the Service

### Preparation
In order to run the service locally, a postgres database, SQS queues, and LocalStack are required. 
These are available through the [docker-compose.yml](docker-compose.yml) file.

To start the required containers through Docker, from the root of the project run 

```
 docker-compose up 
 ```
In order to stop the containers, run
````$xslt
docker-compose down
````

### Running in an IDE

If you are using an IDE, such as IntelliJ, this service can be started by running the `HocsWorkflowApplication` main class. 
The service can then be accessed at ```http://localhost:8091```.

### Building and running without an IDE

This service is built using Gradle. In order to build the project from the command line, run `gradle clean build` from the root of the project.

Alternatively, the corresponding Docker image for this service is available at [quay.io](https://quay.io/repository/ukhomeofficedigital/hocs-workflow).

### Tests

The suite of tests includes unit tests for the resource and services classes, and integration tests. In order to run the integration tests, an instance of postgres must be running.
To run the unit tests in isolation (without the need for a postgres instance running), run the following:
```bash
./gradlew test
```

### Deployment

See the [pipeline](.drone.yml) for the steps involved in the build and deployment.

## Running the HOCS project

The entire set of services can be run in Docker containers from the
 [hocs](https://github.com/UKHomeOffice/hocs) project. Navigate to the `/docker`  directory, then run `./scripts/infrastructure.sh`
to initiate the infrastructure service containers. These include the postgres, SQS, and LocalStack images. 

When the containers are set up and the services have completed starting, then run `./scripts/services.sh`
to launch the set of HOCS micro-services.

To stop and clear the service containers run `./scripts/clean.sh`.

## Camunda Conventions

### Non-modifiable fields

Sometimes identifying fields are required that aren't changeable through the frontend UI to drive system behaviour within processes. To support this the convention that we use is `X[FieldName]`.

## Using the Service

### Versioning

For versioning this project uses [SemVer](https://semver.org/).

### Authors

This project is authored by the Home Office.

### License 

This project is licensed under the MIT license. For details please see [License](LICENSE) 
