# Service Cutter

The Service Cutter suggests a structured way to service decomposition. There is [a tutorial](https://servicecutter.github.io/) providing you with an overview. This readme focuses on installation and development aspects. A comprehensive documentation of the import file is attached in the file `import.md` in this repository.

# Overview

The Service Cutter consists of the following components:

* Editor - a web application, the graphical user interface of the Service Cutter (based on [JHipster](https://jhipster.github.io/)).
* Engine - the RESTful HTTP API offering the core functionality of the service cutter (based on [Spring Boot].(http://projects.spring.io/spring-boot/) and [Jersey](https://jersey.java.net/))
* Samples - contains three sample systems that can be analyzed with the Service Cutter.
   * A tiny booking system with three entities.
   * The domain model of the DDD sample application "[Cargo Tracker](http://dddsample.sourceforge.net/)"
   * An imaginary trading system one might find in a bank.

All source code is released under the terms of the Apache 2.0 license.

## Build and Run

Prerequisite: Maven & JDK 1.8 is installed.

Then run in two different command prompts:
* `cd Engine; mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8090'`
* `cd Editor; mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8080'`

Now you should be able to access the Service Cutter on the following URL: http://localhost:8080

The editor by default expects the engine to be accessible under http://localhost:8090. If not, please add the following JVM parameters to the editor:
* `application.links.engine.host=enginehost`
* `application.links.engine.port=1234`

# Deployment

You have two options to run the Service Cutter:

* Use the embedded Tomcat provided by Spring Boot.
* Deploy the `.war` file into a Java application server.

The required setup and dependencies are demonstrated in the provided Docker configuration.

## Docker

All components can be started using docker. The Docker images can be built using Maven and a docker-compose configuration (`docker-compose.yml`) is provided in the root folder of this repository. Please remember to change the database user passwords in a productive environment!
