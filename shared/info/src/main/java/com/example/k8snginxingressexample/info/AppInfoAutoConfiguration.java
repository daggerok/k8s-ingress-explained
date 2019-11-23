package com.example.k8snginxingressexample.info;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppInfoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppInfo appInfo(Environment environment) {
        return new AppInfo(environment);
    }
}
