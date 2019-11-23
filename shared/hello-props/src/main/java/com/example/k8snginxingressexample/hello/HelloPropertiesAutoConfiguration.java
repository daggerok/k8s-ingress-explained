package com.example.k8snginxingressexample.hello;

import com.example.k8snginxingressexample.hello.props.HelloProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HelloProperties.class)
public class HelloPropertiesAutoConfiguration { }
