# k8s nginx-ingress
Reverse-proxy by nginx-ingress for spring-boot services in k8s

IN PROGRESS

## local

### local hello-variants-service

```bash
./mvnw -f hello-variants-service clean spring-boot:run &
http :8001/api/hello/find-all-hello
http post :8001/actuator/shutdown
```

### local greeting-service

```bash
./mvnw -f greeting-service clean spring-boot:run &
http :8003/api/greeting/find-all-greetings
http :8003/api/greeting/find-all-greetings/max
http post :8003/actuator/shutdown
```

### local frontend

```bash
./mvnw -f frontend clean spring-boot:run &
http :8004/api/greeting/find-all-hello
http :8004/api/greeting/find-all-greetings
http :8004/api/greeting/find-all-greetings/max
http post :8004/actuator/shutdown
```

## resources

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/maven-plugin/)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Spring Data R2DBC [Experimental]](https://docs.spring.io/spring-data/r2dbc/docs/1.0.x/reference/html/#reference)
* [R2DBC example](https://github.com/spring-projects-experimental/spring-boot-r2dbc/tree/master/spring-boot-example-h2)
* [R2DBC Homepage](https://r2dbc.io)

