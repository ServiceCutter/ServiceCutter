The services toolkit consists of the following components:

* Engine - the REST API (Spring Boot, Jersey)
* Editor - a Web App to Edit the model (JHipster)

All source code is released under the terms of the Apache 2.0 license.

## Docker

All components can be started using docker.

```
docker-compose up
```

All docker images need to be built using Maven before using docker-compose.

## REST / HTTP Methods

http://stackoverflow.com/questions/6203231/which-http-methods-match-up-to-which-crud-methods

```
Create = PUT with a new URI
         POST to a base URI returning a newly created URI
Read   = GET
Update = PUT with an existing URI
Delete = DELETE
```