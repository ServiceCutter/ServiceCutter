start the service:

```
spring-boot:run
```

get the engine state:

```
curl -i -H "Accept: application/json" http://localhost:8080/engine
```

start a new process:

```
curl -i -H "Content-Type: application/json" -X PUT http://localhost:8080/engine -d '["foo", "bar"]'
```

import a test file:

```
curl -i -H "Content-Type: application/json" -X PUT http://localhost:8080/engine/import -d @test_domain_model.json
```

# WS docs

Documentation of the WS is generated with a MVN build and available under `target/jaxrs-analyzer/swagger.json`.

# Run using Docker

```
mvn package docker:build
docker run -t -p40000:8080 services-toolkit/engine
docker ps
docker stop xyz
```
