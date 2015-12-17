Start the Engine:

```
mvn spring-boot:run -Drun.jvmArguments='-Dserver.port=8090'
```

get the engine state:

```
curl -i -H "Accept: application/json" http://localhost:8090/engine
```

import an ERM:

```
curl -i -H "Content-Type: application/json" -X POST http://localhost:8090/engine/import -d @../Samples/booking_1_model.json
```

import user representations:

```
curl -i -H "Content-Type: application/json" -X POST http://localhost:8090/engine/import/{systemId}/userrepresentations -d @../Samples/booking_2_user_representations.json
```

get the just created user system:

```
curl -i -H "Content-Type: application/json" -X GET http://localhost:8090/engine/systems/{systemId}
curl -i -H "Content-Type: application/json" -X GET http://localhost:8090/engine/{systemId}/couplingdata
```

# Swagger docs

Documentation of the web service is generated with a maven build and available under `target/jaxrs-analyzer/swagger.json`.
