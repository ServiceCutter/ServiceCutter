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