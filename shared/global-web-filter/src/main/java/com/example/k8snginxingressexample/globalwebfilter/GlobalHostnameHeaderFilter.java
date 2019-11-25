package com.example.k8snginxingressexample.globalwebfilter;

import com.example.k8snginxingressexample.info.AppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class GlobalHostnameHeaderFilter implements WebFilter {

    private final AppInfo info;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("setting hostname from {}", info);
        var hostname = info.getInfo().get("hostname");
        exchange.getResponse().getHeaders().add("X-HOSTNAME", hostname);
        var path = exchange.getRequest().getPath().toString();
        exchange.getResponse().getHeaders().add("X-PATH", path);
        return chain.filter(exchange);
    }
}
