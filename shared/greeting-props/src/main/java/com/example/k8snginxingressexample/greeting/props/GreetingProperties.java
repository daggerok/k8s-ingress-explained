package com.example.k8snginxingressexample.greeting.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "greeting")
public class GreetingProperties {
    private String host;
    private Integer port;
}
