# Service Cutter

The Service Cutter suggests a structured way to service decomposition. There is [a tutorial](https://servicecutter.github.io/) providing you with a functional overview. 

This readme focuses on installation and development aspects. A comprehensive documentation of the import format and all coupling criteria can be found in the [wiki](https://github.com/ServiceCutter/ServiceCutter/wiki) of this repository.

The Service Cutter is based on the Bachelor Thesis by [Lukas Kölbener](https://github.com/koelbener) and [Michael Gysel](https://github.com/gysel).

Build status: [![Build Status](https://travis-ci.org/ServiceCutter/ServiceCutter.svg?branch=master)](https://travis-ci.org/ServiceCutter/ServiceCutter)

# Overview

The Service Cutter consists of the following components:

* **Editor** - a web application, the graphical user interface of the Service Cutter (based on [JHipster](https://jhipster.github.io/)).
* **Engine** - the RESTful HTTP API offering the core functionality of the service cutter (based on [Spring Boot](http://projects.spring.io/spring-boot/) and [Jersey](https://jersey.java.net/))
* **Samples** - contains three sample systems that can be analyzed with the Service Cutter.
   * A tiny booking system with three entities.
   * The domain model of the DDD sample application "[Cargo Tracker](http://dddsample.sourceforge.net/)"
   * An imaginary trading system one might find in a bank.
* **CC-Cards** - contains the coupling criteria cards as `png` files.

All source code is released under the terms of the Apache 2.0 license.

# Build and Run

Prerequisite: JDK 1.8 and Maven is installed and added to the path.

Then run in a command prompt / shell:
```
cd Engine
mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8090'
```

After some time you should see the following line:

    2015-12-15 20:21:55.811  INFO 2976 --- [           main] c.h.s.EngineServiceAppication            : Started EngineServiceAppication in 9.113 seconds (JVM running for 9.946)

Open http://localhost:8090/engine to verify whether the engine is running correctly. You should see:

```json
{"description":"Engine is up and running."}
```

Now we need to install the JHipster development dependencies:

1. Install Node.js [from the Node.js; tested with LTS](http://nodejs.org/) website.
2. Install Yeoman: `npm install -g yo`
3. Install Bower: `npm install -g bower`
4. Install Grunt: `npm install -g grunt-cli`

And in a second command prompt / shell:
```
cd Editor
mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8080'
```

Now you should be able to access the Service Cutter using the following URL: [http://localhost:8080](http://localhost:8080)

The editor by default expects the engine to be accessible under [http://localhost:8090](http://localhost:8090). If not, please add the following JVM parameters to the editor:
* `application.links.engine.host=enginehost`
* `application.links.engine.port=1234`

# Development

We recommend using Grunt and Eclipse (including [Spring Tool Suite](https://spring.io/tools)) for development. 

Information on how to import a JHipster project into Eclipse can be found in the [JHipster documentation](https://jhipster.github.io/configuring_ide_eclipse.html).

# Deployment

You have two options to run the Service Cutter:

* Use the embedded Tomcat provided by Spring Boot.
* Deploy the `.war` file into a Java application server.

The required setup and dependencies are demonstrated in the provided Docker configuration.

## Docker

All components can be built and started using docker and docker compose. Just run `docker-compose up` in the root directory of this repository (you don't have to pre-build the applications).
