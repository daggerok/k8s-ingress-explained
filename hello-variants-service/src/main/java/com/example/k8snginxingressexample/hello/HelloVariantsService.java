package com.example.k8snginxingressexample.hello;

import com.example.k8snginxingressexample.info.AppInfo;
import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Value(staticConstructor = "of")
class HelloResponse {
    private final String key;
    private final String value;
}

@Data
@Table("hello")
@NoArgsConstructor
@AllArgsConstructor(staticName = "allOf")
@RequiredArgsConstructor(staticName = "of")
class Hello {

    @Id
    String identity;

    @NonNull
    String variant;

    HelloResponse toResponse() {
        return HelloResponse.of(identity, variant);
    }
}

interface HelloRepository extends R2dbcRepository<Hello, String> { }

@Service
@RequiredArgsConstructor
class HelloHandler {

    final AppInfo appInfo;
    final HelloRepository helloRepository;

    Mono<ServerResponse> handleFinaAll(ServerRequest request) {
        return ServerResponse.ok().body(helloRepository.findAll().map(Hello::toResponse), HelloResponse.class);
    }

    Mono<ServerResponse> handleFallback(ServerRequest request) {
        return ServerResponse.ok().body(Mono.just(appInfo.getInfo()), Map.class);
    }
}

@Configuration
class HelloRestAPI {

    @Bean
    RouterFunction routes(HelloHandler handlers) {
        return RouterFunctions.route()
                              .nest(path("/"), builder -> builder
                                      .GET("/find-all-hello", handlers::handleFinaAll)
                                      .GET("/info", handlers::handleFallback))
                              .build()
                              .andRoute(path("/**"), handlers::handleFallback);
    }
}

@Controller
@RequiredArgsConstructor
class HelloRSocketAPI {

    final HelloRepository helloRepository;

    @MessageMapping("find-all-hello")
    Flux<HelloResponse> sendAll() {
        return helloRepository.findAll().map(Hello::toResponse);
    }
}

@SpringBootApplication
public class HelloVariantsService {
    public static void main(String[] args) {
        SpringApplication.run(HelloVariantsService.class, args);
    }
}
