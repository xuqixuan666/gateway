package com.mcgoo.gateway.fitler;

import com.mcgoo.common.core.constant.AuthConstants;
import com.mcgoo.common.core.constant.SysConstants;
import com.mcgoo.common.core.result.ResultCode;
import com.mcgoo.gateway.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 全局过滤器，对token的拦截，解析token放入header中，便于下游微服务获取用户信息
 * 分为如下几步：
 *  1、白名单直接放行
 *  2、校验token
 *  3、读取token中存放的用户信息
 *  4、重新封装用户信息，加密成功json数据放入请求头中传递给下游微服务
 */
@Component
@Slf4j
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered {
    /**
     * JWT令牌的服务
     */
    @Autowired
    private TokenStore tokenStore;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String token = req.getHeaders().getFirst(AuthConstants.AUTHORIZATION_KEY);

        // 没有token 或token规则不对则放行交给下面认证管理器处理
        if (StringUtils.isBlank(token) || !StringUtils.startsWithIgnoreCase(token, AuthConstants.JWT_PREFIX)) {
            return chain.filter(exchange);
        }
        token = StringUtils.replace(token, AuthConstants.JWT_PREFIX, "");

        //3 解析token
        try {
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
            Map<String, Object> additionalInformation = oAuth2AccessToken.getAdditionalInformation();
            // 令牌的唯一ID
            String jti = additionalInformation.get(AuthConstants.JWT_JTI).toString();
            String userId = additionalInformation.getOrDefault(SysConstants.USER_ID, "").toString();
            String tenantId = additionalInformation.getOrDefault(SysConstants.TENANT_ID, "").toString();

            ServerHttpRequest builder = req
                    .mutate()
                    .header(SysConstants.USER_ID, userId)
                    .header(SysConstants.TENANT_ID, tenantId)
                    .build();
            return chain.filter(exchange.mutate().request(builder).build());
        } catch (Exception e) {
            log.error("{}", e);
            return ResponseUtils.writeErrorInfo(exchange.getResponse(), ResultCode.INVALID_TOKEN);
        }


    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 对url进行校验匹配
     */
    private boolean checkUrls(List<String> urls,String path){
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String url : urls) {
            if (pathMatcher.match(url,path))
                return true;
        }
        return false;
    }

    /**
     * 从请求头中获取Token
     */
    private String getToken(ServerWebExchange exchange) {
        String tokenStr = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isBlank(tokenStr)) {
            return null;
        }
        String token = tokenStr.split(" ")[1];
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return token;
    }

//    /**
//     * 无效的token
//     */
//    private Mono<Void> invalidTokenMono(ServerWebExchange exchange) {
//        return buildReturnMono(ResultMsg.builder()
//                .code(ResultCode.INVALID_TOKEN.getCode())
//                .msg(ResultCode.INVALID_TOKEN.getMsg())
//                .build(), exchange);
//    }
//
//
//    private Mono<Void> buildReturnMono(ResultMsg resultMsg, ServerWebExchange exchange) {
//        ServerHttpResponse response = exchange.getResponse();
//        byte[] bits = JSON.toJSONString(resultMsg).getBytes(StandardCharsets.UTF_8);
//        DataBuffer buffer = response.bufferFactory().wrap(bits);
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        response.getHeaders().add("Content-Type", "application/json;charset:utf-8");
//        return response.writeWith(Mono.just(buffer));
//    }
}
