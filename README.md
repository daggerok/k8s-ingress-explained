# k8s ingress explained [![Build Status](https://travis-ci.org/daggerok/k8s-ingress-explained.svg?branch=master)](https://travis-ci.org/daggerok/k8s-ingress-explained)
Expose kubernetes services with nginx / traefik ingress for spring-boot services in k8s with skaffold

```
                 +---------------------------------------------+
                 |                        +-------+            |
                 |      K8S               | hello |<---+       |
  O       +---------+                     +-------+    |       |
 /|\ <--> | Ingress | <---+                            |       |
 / \      +---------+     |    +----------+      +----------+  |
                 |        +--> | frontend | ---> | greeting |  |
                 |             +----------+      +----------+  |
                 +---------------------------------------------+
```

## table of content
1. [version: 0.0.1]()
   * [local workflow](#local)
     * [hello](#local-hello-variants-service)
     * [greeting](#local-greeting-service)
     * [frontend](#local-frontend)
   * [maven-release-plugin](#release)
1. [version: 0.0.2]()
   * [gib-maven-plugin](#gib)
     * [fat jar configuration](#fat-jar-configuration)
     * [thin jar configuration](#thin-jar-configuration)
     * [debug jib images](#debug-jib)
   * [fabric8 docker-maven-plugin](#fabric8)
     * [configuration](#fabric8-configuration)
     * [build, run and test](#run-with-fabric8)
   * [dkanejs docker-compose-maven-plugin](#dkanejs-docker-compose-maven-plugin)
     * [configuration](#dkanejs-configuration)
     * [build, run and test](#run-using-dkanejs-docker-compose-maven-plugin)
1. [0.0.3: kubernetes skaffold workflow](#k8s--skaffold)
   * [k8s resources](#k8s)
   * [skaffold docker for mac / windows](#skaffold)
1. [version: 0.0.4]()
   * [k8s nginx ingress for Docker for Mac / Windows](#k8s-nginx-ingress-for-docker-for-mac)
     * [nginx ingress default backend](#nginx-ingress-default-backend)
     * [nginx ingress rewrite-target](#nginx-ingress-rewrite-target)
   * [k8s traefik ingress for k3d k3s](#k8s-traefik-ingress-for-k3d-k3s)
     * [traefik ingress default backend](#traefik-ingress-default-backend)
     * [traefik ingress PathPrefixStrip](#traefik-ingress-pathprefixstrip)
1. [resources](#resources)

## features

* [k8s, k3s, k3d]()
* [skaffold in multi-project]()
* [fabric8 docker-compose wait]()
* [docker-compose with fabric8 docker-maven-plugin]()
* [jib thin jar]()
* [jib fat jar]()
* [spring.factories auto-configurations]()
* [spring-boot maven multi-module project]()

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
    --network docker_k8s-ingress-explained \
    daggerok/k8s-ingress-explained-frontend:latest
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
                <alias>frontend</alias>
                <external>
                    <type>compose</type>
                    <basedir>src/main/docker</basedir>
                    <composeFile>docker-compose.yml</composeFile>
                </external>
                <run>
                    <wait>
                        <http>
                            <url>http://127.0.0.1:8004/find-all-greetings/ready</url>
                            <method>GET</method>
                            <status>200</status>
                        </http>
                        <time>100000</time>
                    </wait>
                </run>
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
        <composeFile>${project.basedir}/src/main/docker/docker-compose.yaml</composeFile>
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

## k8s + skaffold

### k8s

prepared kubernetes resources can be found in [k8s](./k8s/) folder

### skaffold

with k8s based on docker for mac / windows

```bash
brew reinstall skaffold
skaffold init --skip-build
vi ./skaffold.yaml
# ...
skaffold dev --cache-artifacts=false
http :30001/info
http :30003/info
http :30004/info
http :30004/find-all-hello
http :30004/find-all-greetings
# ...
skaffold delete
```

## k8s nginx ingress for Docker for Mac

### nginx ingress default backend

Default backend for fronted application or main micro-services gateway,
see `k8s/ingress/ingress-docker-for-mac-nginx-default-backend.yaml` k8s resources file for details

```bash
# enable Kubernetes cluster in DOcker for Mac / Windows...
# watch pods...
kubectl get pods --all-namespaces -o wide -w &
# deploy cloud native micro-services apps and ingress
kubectl apply -f k8s/
kubectl apply -f k8s/ingress/ingress-docker-for-mac-nginx-default-backend.yaml
# wait more...
sleep 120s
# test if everything is working
http :/actuator/health
http :/actuator/info
http :/
http :/info
http :/find-all-hello
http :/find-all-greetings
http :/find-all-greetings/ololo-trololo
# cleanup
kubectl delete -f k8s/
kubectl delete -f k8s/ingress/
```

### nginx ingress rewrite-target

Default backend with some rewrite targets,
see `k8s/ingress/ingress-docker-for-mac-nginx-rewrite-target.yaml` k8s resources file for details

```bash
# enable Kubernetes cluster in DOcker for Mac / Windows...
# watch pods...
kubectl get pods --all-namespaces -o wide -w &
# deploy cloud native micro-services apps and ingress
kubectl apply -f k8s/
kubectl apply -f k8s/ingress/ingress-docker-for-mac-nginx-rewrite-target.yaml
# wait more...
sleep 120s
# test if everything is working
http :/actuator/health
http :/actuator/info
http :/
http :/info
http :/find-all-hello
http :/find-all-greetings
http :/find-all-greetings/ololo-trololo
# cleanup
kubectl delete -f k8s/
kubectl delete -f k8s/ingress/
```

## k8s traefik ingress for k3d k3s

### traefik ingress default backend

Default backend for fronted application or main micro-services gateway,
see `k8s/ingress/ingress-traefik-default-backend.yaml` k8s resources file for details

```bash
# create k3s k8s cluster by using k3d tool (k3s in docker) with published port: 80
k3d create --name k3s --api-port 6551 --publish 80:80 --workers 1
# wait few moments
sleep 5s
# point kubectl to created k3s
export KUBECONFIG="$(k3d get-kubeconfig --name='k3s')"
# watch pods
kubectl get pods --all-namespaces -o wide -w &
# deploy cloud native micro-services apps and ingress
kubectl apply -f k8s/
kubectl apply -f k8s/ingress/ingress-traefik-default-backend.yaml
# wait more...
sleep 120s
# test if everything is working
http :/actuator/health
http :/actuator/info
http :/
http :/info
http :/find-all-hello
http :/find-all-greetings
http :/find-all-greetings/ololo-trololo
# cleanup
kubectl delete -f k8s/
kubectl delete -f k8s/ingress/
k3d stop --name=k3s -a
docker rm -f -v `docker ps -a -q`
rm -rf ~/.config/k3d/k3s
```

### traefik ingress PathPrefixStrip

Default backend with some rewrite targets,
see `k8s/ingress/ingress-traefik-path-prefix-strip.yaml` k8s resources file for details

```bash
# create k3s k8s cluster by using k3d tool (k3s in docker) with published port: 80
k3d create --name k3s --api-port 6551 --publish 80:80 --workers 1
# wait few moments
sleep 5s
# point kubectl to created k3s
export KUBECONFIG="$(k3d get-kubeconfig --name='k3s')"
# watch pods
kubectl get pods --all-namespaces -o wide -w &
# deploy cloud native micro-services apps and ingress
kubectl apply -f k8s/
kubectl apply -f k8s/ingress/ingress-traefik-path-prefix-strip.yaml
# wait more...
sleep 120s
# test if everything is working
http :/info
http :/greeting/info
http :/hello/info
# cleanup
kubectl delete -f k8s/
kubectl delete -f k8s/ingress/
k3d stop --name=k3s -a
docker rm -f -v `docker ps -a -q`
rm -rf ~/.config/k3d/k3s
```

Check [k8s](./k8s/) folder and [.travis.yml](.travis.yml) file for details...

## release

```bash
./mvnw release:clean release:prepare release:perform --batch-mode
# ./mvnw release:rollback
```

## resources

* [traefik-ingress rewrite](https://docs.traefik.io/v1.7/user-guide/kubernetes/#path-based-routing)
* [GitHub: rancher/k3d](https://github.com/rancher/k3d)
* [GitHub: rancher/k3s](https://github.com/rancher/k3s)
* [k3s reference](https://rancher.com/docs/k3s/latest/en/)
* [nginx-ingress rewrite](https://kubernetes.github.io/ingress-nginx/examples/rewrite/)
* [ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)
* [nginx-ingress: common](https://kubernetes.github.io/ingress-nginx/deploy/#prerequisite-generic-deployment-command)
* [nginx-ingress: docker for mac / windows](https://kubernetes.github.io/ingress-nginx/deploy/#docker-for-mac)
* [nginx-ingress: bare-metal](https://kubernetes.github.io/ingress-nginx/deploy/#bare-metal)
* [k8s ingress controllers](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/)
* [k8s ingress minikube](https://kubernetes.io/docs/tasks/access-application-cluster/ingress-minikube/)
* [k8s service type: LoadBalancer](https://kubernetes.io/docs/concepts/services-networking/service/#loadbalancer)
* [k8s service type: NodePort](https://kubernetes.io/docs/concepts/services-networking/service/#nodeport)
* [Quarkus skaffold](https://medium.com/@dav.poletti/quarkus-development-mode-with-kubernetes-and-skaffold-ea17621e168)
* [multi skaffold](https://github.com/GoogleContainerTools/skaffold/blob/master/examples/jib-multimodule/skaffold.yaml)
* [Spring WebFlux WebFilter](https://www.baeldung.com/spring-webflux-filters#1-webfilter)
* [spring-boot k8s](https://dzone.com/articles/developing-a-spring-boot-application-for-kubernete-4)
* [spring k8s](https://dzone.com/articles/quick-guide-to-microservices-with-kubernetes-sprin)
* [fabric8 docker-compose wait](https://github.com/fabric8io/docker-maven-plugin/issues/1118)
* [Shell in jib](https://github.com/GoogleContainerTools/jib/blob/master/docs/faq.md#where-is-bash)
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
