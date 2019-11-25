package com.example.k8snginxingressexample.globalwebfilter;

import com.example.k8snginxingressexample.info.AppInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class GlobalWebFilterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebFilter webFilter(AppInfo info) {
        return new GlobalHostnameHeaderFilter(info);
    }
}
