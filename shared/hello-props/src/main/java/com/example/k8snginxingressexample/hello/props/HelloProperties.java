package com.example.k8snginxingressexample.hello.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "hello")
public class HelloProperties {
    private String host;
    private Integer port;
}
