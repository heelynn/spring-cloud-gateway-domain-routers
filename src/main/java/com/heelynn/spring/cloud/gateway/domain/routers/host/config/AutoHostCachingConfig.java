package com.heelynn.spring.cloud.gateway.domain.routers.host.config;

import com.heelynn.spring.cloud.gateway.domain.routers.host.HostCachingRouteLocator;
import com.heelynn.spring.cloud.gateway.domain.routers.host.HostRouteDefinitionRouteLocator;
import com.heelynn.spring.cloud.gateway.domain.routers.host.HostRoutePredicateHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.CompositeRouteLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author heelynn
 *
 * 自定义路由配置，为了实现根据host（域名）选择路由，并缓存路由信息，提高路由配置项不断增加导致的O(n)时间复杂度。
 */
@ConditionalOnProperty(name = "spring.cloud.gateway.host-routing.enabled", matchIfMissing = true, havingValue = "true")
@Configuration
@ComponentScan( excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RouteDefinitionRouteLocator.class),
})
public class AutoHostCachingConfig {

    @Bean
    public RouteLocator hostRouteDefinitionRouteLocator(GatewayProperties properties,
                                                    List<GatewayFilterFactory> gatewayFilters, List<RoutePredicateFactory> predicates,
                                                    RouteDefinitionLocator routeDefinitionLocator, ConfigurationService configurationService) {
        return new HostRouteDefinitionRouteLocator(routeDefinitionLocator, predicates, gatewayFilters, properties,
                configurationService);
    }


    @Bean
    @Primary
    // TODO: property to disable composite?
    public RouteLocator cachedCompositeRouteLocator(List<RouteLocator> routeLocators) {
        return new HostCachingRouteLocator(new CompositeRouteLocator(Flux.fromIterable(routeLocators)));
    }



    @Bean
    public HostRoutePredicateHandlerMapping routePredicateHandlerMapping(FilteringWebHandler webHandler,
                                                                         RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment) {
        return new HostRoutePredicateHandlerMapping(webHandler, routeLocator, globalCorsProperties, environment);
    }


}
