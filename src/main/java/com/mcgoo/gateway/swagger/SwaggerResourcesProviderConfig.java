package com.mcgoo.gateway.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Primary
public class SwaggerResourcesProviderConfig implements SwaggerResourcesProvider {

    // SWAGGER3默认的URL后缀
    public static final String SWAGGER3URL = "/v3/api-docs";
    public static final String SWAGGER_VERSION = "3.0";

    // 网关路由
    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private GatewayProperties gatewayProperties;

    // 聚合其他服务接口
    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resourceList = new ArrayList<>();
        Set<String> routes = new HashSet<>();
        // 获取网关中配置的route
//        routeLocator.getRoutes().subscribe(route -> route.getUri().getHost());

        routeLocator.getRoutes()
                .filter(route -> route.getUri().getHost() != null)
//                .filter(route -> !self.equals(route.getUri().getHost()))
                .subscribe(route -> routes.add(route.getUri().getHost()));

        routes.forEach(instance -> {
            // 拼接url
            String url = "/" + instance.toLowerCase() + SWAGGER3URL;
                SwaggerResource swaggerResource = new SwaggerResource();
                swaggerResource.setUrl(url);
                swaggerResource.setName(instance);
            resourceList.add(swaggerResource);
        });
//        gatewayProperties.getRoutes().stream().filter(routeDefinition -> routes.contains(routeDefinition.getId()))
//                .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
//                        .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName()))
//                        .forEach(predicateDefinition -> {
//                                    String id = routeDefinition.getId();
//                                    String location = id + SWAGGER3URL;
//                                    resourceList.add(swaggerResource(id, location));
//
//                                }
//                        ));
        return resourceList;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(SWAGGER_VERSION);
        return swaggerResource;
    }
}
