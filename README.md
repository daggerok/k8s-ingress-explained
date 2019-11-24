# k8s nginx-ingress
Reverse-proxy by nginx-ingress for spring-boot services in k8s

IN PROGRESS

## table of content
1. [local workflow](#local)
   * [hello](#local-hello-variants-service)
   * [greeting](#local-greeting-service)
   * [frontend](#local-frontend)
1. [gib-maven-plugin](#gib)
   * [fat jar configuration](#fat-jar-configuration)
   * [thin jar configuration](#thin-jar-configuration)
   * [debug jib images](#debug-jib)
1. [fabric8 docker-maven-plugin](#fabric8)
   * [configuration](#fabric8-configuration)
   * [build, run and test](#run-with-fabric8)
1. [dkanejs docker-compose-maven-plugin](#dkanejs-docker-compose-maven-plugin)
   * [configuration](#dkanejs-configuration)
   * [build, run and test](#run-using-dkanejs-docker-compose-maven-plugin)
1. [maven-release-plugin](#release)
1. [resources](#resources)

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

## GIB

**requires: Docker**

* Add Google jib maven plugin in _pom.xml_ file for spring-boot all applications / services

### fat jar configuration

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>${jib-maven-plugin.version}</version>
    <configuration>
        <containerizingMode>packaged</containerizingMode>
        <container>
            <appRoot>/tmp</appRoot>
            <useCurrentTimestamp>true</useCurrentTimestamp>
            <entrypoint>
                <shell>bash</shell>
                <option>-c</option>
                <arg>java -cp /tmp/classpath:/tmp/*.jar</arg>
            </entrypoint>
            <args>
                <arg>/bin/bash</arg>
            </args>
        </container>
        <from>
            <image>openjdk:11.0.5-jre-stretch</image>
        </from>
        <to>
            <image>daggerok/${project.parent.artifactId}-${project.artifactId}</image>
            <tags>
                <tag>${project.version}</tag>
                <tag>latest</tag>
            </tags>
        </to>
    </configuration>
</plugin>
```

### thin jar configuration

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>${jib-maven-plugin.version}</version>
    <configuration>
        <allowInsecureRegistries>true</allowInsecureRegistries>
        <container>
            <creationTime>USE_CURRENT_TIMESTAMP</creationTime>
        </container>
        <to>
            <image>daggerok/${project.parent.artifactId}-${project.artifactId}</image>
            <tags>
                <tag>${project.version}</tag>
                <tag>latest</tag>
            </tags>
        </to>
    </configuration>
</plugin>
```

### debug jib

make sure you are using:

```xml
<from>
    <!--<image>gcr.io/distroless/java:11</image>-->
    <image>gcr.io/distroless/java:11-debug</image>
</from>
```

then build jib image and run command:

```bash
docker run -it \
    --entrypoint /busybox/sh \
    --network docker_k8s-nginx-ingress-example \
    daggerok/k8s-nginx-ingress-example-frontend:latest
```

## fabric8

### fabric8 configuration

```xml
<plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>${docker-maven-plugin.version}</version>
    <configuration>
        <follow>false</follow>
        <verbose>true</verbose>
        <useColor>true</useColor>
        <logDate>default</logDate>
        <autoPull>always</autoPull>
        <keepRunning>false</keepRunning>
        <watchInterval>500</watchInterval>
        <allContainers>true</allContainers>
        <removeVolumes>true</removeVolumes>
        <imagePullPolicy>IfNotPresent</imagePullPolicy>
        <autoCreateCustomNetworks>true</autoCreateCustomNetworks>
        <images>
            <image>
                <alias>${project.parent.artifactId}</alias>
                <!-- The <run> part is taken from compose -->
                <external>
                    <type>compose</type>
                </external>
            </image>
        </images>
    </configuration>
</plugin>
```

### run with fabric8

build jib docker images and run everything using `fabric8` maven docker-compose

```bash
./mvnw clean package jib:dockerBuild
./mvnw -f docker-compose -Pstart
http :8004/find-all-hello
http :8004/find-all-greetings
./mvnw -f docker-compose -Pstop
```

## dkanejs docker-compose-maven-plugin

### dkanejs configuration

```xml
<plugin>
    <groupId>com.dkanejs.maven.plugins</groupId>
    <artifactId>docker-compose-maven-plugin</artifactId>
    <version>${docker-compose-maven-plugin.version}</version>
    <configuration>
        <detail>true</detail>
        <verbose>false</verbose>
        <detachedMode>true</detachedMode>
        <ignorePullFailures>true</ignorePullFailures>
        <removeImagesType>local</removeImagesType>
        <removeImages>true</removeImages>
        <removeOrphans>true</removeOrphans>
        <removeVolumes>true</removeVolumes>
        <composeFile>${project.basedir}/src/main/docker/docker-compose.yml</composeFile>
    </configuration>
</plugin>
```

### run using dkanejs docker-compose-maven-plugin

build jib docker images and run everything using `dkanejs` docker-compose-maven-plugin

```bash
./mvnw clean package jib:dockerBuild
./mvnw -f docker-compose -Pup
http :8004/find-all-hello
http :8004/find-all-greetings
./mvnw -f docker-compose -Pdown
```

## release

```bash
./mvnw release:clean release:prepare release:perform --batch-mode
# ./mvnw release:rollback
```

## resources

* [Shell in jib]( https://github.com/GoogleContainerTools/jib/blob/master/docs/faq.md#where-is-bash)
* [Using jib](https://github.com/GoogleContainerTools/jib/blob/master/docs/faq.md#what-would-a-dockerfile-for-a-jib-built-image-look-like)
* [Using skaffold](https://github.com/daggerok/boot-skaffold)
* [Using maven-release-plugin](https://github.com/daggerok/es-cqrs)
* [R2DBC H2](https://github.com/spring-projects-experimental/spring-boot-r2dbc#h2-embedded)
* [R2DBC example](https://github.com/spring-projects-experimental/spring-boot-r2dbc/tree/master/spring-boot-example-h2)
* [R2DBC Homepage](https://r2dbc.io)
* [Spring Data R2DBC [Experimental]](https://docs.spring.io/spring-data/r2dbc/docs/1.0.x/reference/html/#reference)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/maven-plugin/)
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
