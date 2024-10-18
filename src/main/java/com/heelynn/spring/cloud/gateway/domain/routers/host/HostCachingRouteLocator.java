package com.heelynn.spring.cloud.gateway.domain.routers.host;

import com.heelynn.spring.cloud.gateway.domain.routers.ICachingRouteLocatorCustom;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author heelynn
 *
 * 用域名做前置条件，缓存，减少RountingRule的计算量
 * 避免O(n)的时间复杂度
 * 修改刷新策略，以域名为条件刷新路由
 */
public class HostCachingRouteLocator extends CachingRouteLocator implements ICachingRouteLocatorCustom {
        //implements ICachingRouteLocatorCustom {


    // 以域名为条件，缓存路由的Reactor Flux
    private Map<String, Flux<Route>> hostRoutes = new ConcurrentHashMap<>();
    // 以域名为条件，缓存路由的List，用于刷新路由（刷新成Reactor Flux）
    private Map<String, List<Route>> hostRoutesList = new ConcurrentHashMap<>();


    public HostCachingRouteLocator(RouteLocator delegate) {
        super(delegate);
    }

    /**
     * getRoutes方法，根据域名获取对应的路由
     *
     * @param exchange
     * @return
     */
    @Override
    public Flux<Route> getRoutes(ServerWebExchange exchange){
        String host = exchange.getRequest().getHeaders().getFirst("Host");
        if (host == null || host.isEmpty()){
            host = DEFAULT_KEY;
        }
        if(hostRoutes.containsKey(host)){
            return hostRoutes.get(host);
        }else{
            return hostRoutes.get(DEFAULT_KEY);
        }
    }

    @Override
    public void onApplicationEvent(RefreshRoutesEvent event) {
        super.onApplicationEvent(event);
        // 刷新路由，根据域名刷新
        refreshToReactorRoutes();
    }



    private void addRoute( Route route){
        Object hostObj = route.getMetadata().get(HostRouteDefinitionRouteLocator.METADATA_HOST);
        String host = hostObj == null? "" : hostObj.toString();
        if(host.isEmpty()){
            host = DEFAULT_KEY;
        }
        if(!hostRoutesList.containsKey(host)){
            hostRoutesList.put(host, new LinkedList<>() {
            });
        }
        hostRoutesList.get(host).add(route);
    }


    /**
     * 刷新路由，根据域名刷新
     */
    private void refreshToReactorRoutes(){
        //获取全部路由，并添加到hostRoutesList中
        super.getRoutes().doOnNext(
                route -> addRoute(route)
        ).subscribe();
        // 设置临时路由
        Map<String, Flux<Route>> hostRoutesTemp = new ConcurrentHashMap<>(hostRoutesList.size());
        // 装填路由
        for(Map.Entry<String, List<Route>> entry : hostRoutesList.entrySet()) {
            hostRoutesTemp.put(entry.getKey(), Flux.fromIterable(entry.getValue()));
        }
        // 刷新路由
        Map<String, Flux<Route>> hostRoutesClean = this.hostRoutes;
        this.hostRoutes = hostRoutesTemp;
        // 清理缓存
        hostRoutesClean.clear();
        hostRoutesList.clear();
    }
}
