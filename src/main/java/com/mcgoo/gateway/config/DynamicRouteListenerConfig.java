package com.mcgoo.gateway.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.mcgoo.gateway.service.DynamicRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 动态路由配置
 */
@Component
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class DynamicRouteListenerConfig {

    private final DynamicRouteService dynamicRouteService;
    @Value("${spring.cloud.nacos.server-addr}")
    private String serverAddr;
    @Value("${spring.cloud.nacos.config.namespace}")
    private String namespace;
    @Value("${spring.cloud.nacos.config.name}")
    private String dataId;
    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String group;


    @PostConstruct
    public void initListener() throws NacosException {
        log.info("初始化并监听路由配置");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        ConfigService configService = NacosFactory.createConfigService(properties);
        String config = configService.getConfigAndSignListener(dataId, group, 3000, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }
            @Override
            public void receiveConfigInfo(String configInfo) {
                dynamicRouteService.refresh(JSON.parseArray(configInfo, RouteDefinition.class));
            }
        });
        dynamicRouteService.publish(JSON.parseArray(config, RouteDefinition.class));
    }
}


