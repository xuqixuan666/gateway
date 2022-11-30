package com.mcgoo.gateway.service;

import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

public interface DynamicRouteService {

    /**
     * 发布路由配置
     * @param routeDefinitions
     */
    void publish(List<RouteDefinition> routeDefinitions);

    /**
     * 刷新路由配置
     *
     * @param routeDefinitions
     */
    void refresh(List<RouteDefinition> routeDefinitions);


}
