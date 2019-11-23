package com.example.k8snginxingressexample.info;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AppInfo {

    private final Environment environment;

    public Map<String, String> getInfo() {
        var name = environment.getProperty("spring.application.name",
                                           "undefined, please configure spring.application.name property");
        var hostname = Optional.ofNullable(environment.getProperty("hostname", System.getenv("HOSTNAME")))
                               .orElse(UUID.randomUUID().toString());
        return Map.of("name", name,
                      "hostname", hostname);
    }
}
