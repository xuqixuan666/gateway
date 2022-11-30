package com.mcgoo.gateway.service.impl;

import com.mcgoo.gateway.service.DynamicRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteServiceImpl implements DynamicRouteService, ApplicationEventPublisherAware {

    /**
     * 路由定义操作
     */
    private final RouteDefinitionWriter routeDefinitionWriter;
    /**
     * 事件发布者
     */
    private ApplicationEventPublisher applicationEventPublisher;

    private final List<String> ROUTE_IDS = new ArrayList<>();

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 发布路由配置
     * @param routeDefinitions
     */
    @Override
    public void publish(List<RouteDefinition> routeDefinitions) {
        if (CollectionUtils.isEmpty(routeDefinitions)) {
           log.warn("路由配置列表为空，无法发布");
           return;
        }
        routeDefinitions.forEach(route -> {
            ROUTE_IDS.add(route.getId());
            routeDefinitionWriter.save(Mono.just(route)).subscribe();
        });
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("路由配置发布成功，size:{}", routeDefinitions.size());
    }

    /**
     * 刷新路由配置
     * @param routeDefinitions
     */
    @Override
    public void refresh(List<RouteDefinition> routeDefinitions) {
        // 清空路由
        ROUTE_IDS.forEach(id -> {
            routeDefinitionWriter.delete(Mono.just(id)).subscribe();
        });
        ROUTE_IDS.clear();
        if (!CollectionUtils.isEmpty(routeDefinitions)) {
            routeDefinitions.forEach(route -> {
                ROUTE_IDS.add(route.getId());
                routeDefinitionWriter.save(Mono.just(route)).subscribe();
            });
        }
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("刷新路由配置成功，size：{}", routeDefinitions.size());
    }

}
