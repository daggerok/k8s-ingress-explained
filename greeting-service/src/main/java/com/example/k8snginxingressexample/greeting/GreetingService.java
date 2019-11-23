package com.example.k8snginxingressexample.greeting;

import com.example.k8snginxingressexample.greeting.resp.GreetingResponse;
import com.example.k8snginxingressexample.hello.props.HelloProperties;
import com.example.k8snginxingressexample.hello.resp.HelloResponse;
import com.example.k8snginxingressexample.info.AppInfo;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Configuration
class RSocketConfig {

    @Bean
    Mono<RSocketRequester> rs(HelloProperties props,
                              RSocketRequester.Builder builder) {

        return builder.dataMimeType(MediaType.APPLICATION_JSON)
                      .connectTcp(props.getHost(), props.getPort())
                      .retryBackoff(3, Duration.ofSeconds(2));
    }
}

@Log4j2
@RestController
@RequiredArgsConstructor
class HelloServicesApi {

    final AppInfo appInfo;
    final Mono<RSocketRequester> rs;

    @GetMapping("/info")
    Mono<Map<String, String>> info() {
        return Mono.just(appInfo.getInfo());
    }

    @GetMapping({ "/find-all-greetings", "/find-all-greetings/{name}" })
    Flux<GreetingResponse> findAll(@PathVariable("name") Optional<String> maybeName) {
        return rs.flatMapMany(rr -> rr.route("find-all-hello")
                                      .data(Mono.empty())
                                      .retrieveFlux(HelloResponse.class))
                 .map(hr -> String.format("%s, %s!", hr.getValue(), maybeName.orElse("Buddy")))
                 .map(GreetingResponse::of);
    }
}

@SpringBootApplication
@EnableConfigurationProperties(HelloProperties.class)
public class GreetingService {
    public static void main(String[] args) {
        SpringApplication.run(GreetingService.class, args);
    }
}
