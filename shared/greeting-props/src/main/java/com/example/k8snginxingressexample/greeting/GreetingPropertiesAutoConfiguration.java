package com.example.k8snginxingressexample.greeting;

import com.example.k8snginxingressexample.greeting.props.GreetingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GreetingProperties.class)
public class GreetingPropertiesAutoConfiguration { }
