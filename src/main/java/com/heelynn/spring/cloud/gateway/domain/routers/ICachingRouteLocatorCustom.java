package com.heelynn.spring.cloud.gateway.domain.routers;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/**
 * @author heelynn
 * @date 2024/8/27 14:38
 * <p>
 * 自定义路由定位器接口
 * 使用某种维度进行路由缓存
 */
public interface ICachingRouteLocatorCustom {

    String DEFAULT_KEY = "default_key_0dew32";

    /**
     *
     * 根据key获取路由 Reactive Stream
     *
     * @param exchange
     * @return
     */
    Flux<Route> getRoutes(ServerWebExchange exchange);

}
