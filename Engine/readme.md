start the service:

```
mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8090'
```

get the engine state:

```
curl -i -H "Accept: application/json" http://localhost:8090/engine
```

import a domain model:

```
curl -i -H "Content-Type: application/json" -X PUT http://localhost:8090/engine/import -d @domain_model.json
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
