package com.example.k8snginxingressexample.frontend;

import com.example.k8snginxingressexample.greeting.props.GreetingProperties;
import com.example.k8snginxingressexample.greeting.resp.GreetingResponse;
import com.example.k8snginxingressexample.hello.props.HelloProperties;
import com.example.k8snginxingressexample.hello.resp.HelloResponse;
import com.example.k8snginxingressexample.info.AppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;



@Configuration
@ComponentScan(basePackageClasses = HelloResponse.class)
class RSocketConfig {

    @Bean
    Mono<RSocketRequester> helloRs(HelloProperties props, RSocketRequester.Builder builder) {
        return builder.dataMimeType(MediaType.APPLICATION_JSON)
                      .connectTcp(props.getHost(), props.getPort())
                      .retryBackoff(3, Duration.ofSeconds(2));
    }
}

@Component
@RequiredArgsConstructor
class HelloClient {

    final Mono<RSocketRequester> helloRs;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Object> helloSteam = helloRs.flatMapMany(rr -> rr.route("find-all-hello")
                                                              .data(Mono.empty())
                                                              .retrieveFlux(HelloResponse.class));
        return ServerResponse.ok().body(helloSteam, HelloResponse.class);
    }
}

@Configuration
class WebClientCfg {

    @Bean
    WebClient webClient(GreetingProperties props) {
        return WebClient.builder()
                        .baseUrl(String.format("http://%s:%s", props.getHost(), props.getPort()))
                        .build();
    }
}

@Component
@RequiredArgsConstructor
class GreetingClient {

    final WebClient webClient;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Object> greetingStream = webClient.get()
                                               .uri("/find-all-greetings")
                                               .exchange()
                                               .retryBackoff(5, Duration.ofSeconds(3))
                                               .flatMapMany(rr -> rr.bodyToFlux(GreetingResponse.class));
        return ServerResponse.ok().body(greetingStream, GreetingResponse.class);

    }

    public Mono<ServerResponse> findAllNamed(ServerRequest request) {
        var name = request.pathVariable("name");
        Flux<Object> greetingStream = webClient.get()
                                               .uri("/find-all-greetings/{name}", name)
                                               .exchange()
                                               .retryBackoff(5, Duration.ofSeconds(3))
                                               .flatMapMany(rr -> rr.bodyToFlux(GreetingResponse.class));
        return ServerResponse.ok().body(greetingStream, GreetingResponse.class);
    }
}

@Log4j2
@Configuration
@RequiredArgsConstructor
class FrontendRoutes {

    final AppInfo appInfo;
    final HelloClient helloClient;
    final GreetingClient greetingClient;

    @Bean
    RouterFunction routes() {
        HandlerFunction<ServerResponse> infoHandler = request ->
                ServerResponse.ok().body(Mono.just(appInfo.getInfo()), Map.class);
        return RouterFunctions.route()
                              .GET("/find-all-greetings/{name}", greetingClient::findAllNamed)
                              .GET("/find-all-greetings", greetingClient::findAll)
                              .GET("/find-all-hello", helloClient::findAll)
                              .GET("/info", infoHandler)
                              .build()
                              .andRoute(path("/*"), infoHandler);
    }

    @GetMapping("/*")
    Mono<Map<String, String>> fallback(ServerWebExchange exchange) {
        log.info(exchange.getRequest().getPath());
        return Mono.just(appInfo.getInfo());
    }
}

@SpringBootApplication
public class FrontendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
