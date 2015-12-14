# Service Cutter

TODO: add abstract?

The Service Cutter consists of the following components:

* Editor - a web application, the graphical user interface of the Service Cutter (based on [JHipster](https://jhipster.github.io/)).
* Engine - the RESTful HTTP API offering the core functionality of the service cutter (based on [Spring Boot].(http://projects.spring.io/spring-boot/) and [Jersey](https://jersey.java.net/))
* Samples - contains three sample systems that can be analyzed with the Service Cutter.
   * A tiny booking system with three entities.
   * The domain model of the DDD sample application "[Cargo Tracker](http://dddsample.sourceforge.net/)"
   * An imaginary trading system one might find in a bank.

All source code is released under the terms of the Apache 2.0 license.

## Build and Run

* prerequisite: maven & jdk 1.8
run in two command lines!
* `cd Engine; mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8090'`
* `cd Editor; mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8080'`

The editor by default expects the engine to be accessible under http://localhost:8090. if not, add the following JVM parameters to the editor:
* `application.links.engine.host=enginehost`
* `application.links.engine.port=1234`

## Simple Example

* load domain model
* load use case
* load characteristics
* load related groups

## Docker

All components can be started using docker. The Docker images can be built using Maven and a docker-compose configuration is provided in the root folder of the repository. 

## REST / HTTP Methods

TODO remove?

http://stackoverflow.com/questions/6203231/which-http-methods-match-up-to-which-crud-methods

```
Create = PUT with a new URI
         POST to a base URI returning a newly created URI
Read   = GET
Update = PUT with an existing URI
Delete = DELETE
```