package com.mcgoo.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 网关鉴权管理器
 */
@Component
@Slf4j
public class ResourceServerManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {
//        ServerHttpRequest request = authorizationContext.getExchange().getRequest();
//        if (request.getMethod() == HttpMethod.OPTIONS) { // 预检请求放行
//            return Mono.just(new AuthorizationDecision(true));
//        }
//        PathMatcher pathMatcher = new AntPathMatcher();
//        String method = request.getMethodValue();
//        String path = request.getURI().getPath();
//        String restfulPath = method + ":" + path; // RESTFul接口权限设计 @link https://www.cnblogs.com/haoxianrui/p/14961707.html
//
//        // 如果token以"bearer "为前缀，到此方法里说明JWT有效即已认证，其他前缀的token则拦截
//        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_KEY);
//        if (StrUtil.isNotBlank(token) && StrUtil.startWithIgnoreCase(token, SecurityConstants.JWT_PREFIX) ) {
//            if (pathMatcher.match(SecurityConstants.APP_API_PATTERN, path)) {
//                // 移动端请求只需认证，无需后续鉴权
//                return Mono.just(new AuthorizationDecision(true));
//            }
//        } else {
//            return Mono.just(new AuthorizationDecision(false));
//        }
//
//
//        /**
//         * 鉴权开始
//         *
//         * 缓存取 [URL权限-角色集合] 规则数据
//         * urlPermRolesRules = [{'key':'GET:/api/v1/users/*','value':['ADMIN','TEST']},...]
//         */
//        Map<String, Object> urlPermRolesRules = redisTemplate.opsForHash().entries(GlobalConstants.URL_PERM_ROLES_KEY);
//
//        // 根据请求路径判断有访问权限的角色列表
//        List<String> authorizedRoles = new ArrayList<>(); // 拥有访问权限的角色
//        boolean requireCheck = false; // 是否需要鉴权，默认未设置拦截规则不需鉴权
//
//        for (Map.Entry<String, Object> permRoles : urlPermRolesRules.entrySet()) {
//            String perm = permRoles.getKey();
//            if (pathMatcher.match(perm, restfulPath)) {
//                List<String> roles = Convert.toList(String.class, permRoles.getValue());
//                authorizedRoles.addAll(Convert.toList(String.class, roles));
//                if (requireCheck == false) {
//                    requireCheck = true;
//                }
//            }
//        }
//        if (requireCheck == false) {
//            return Mono.just(new AuthorizationDecision(true));
//        }
//
//        // 判断JWT中携带的用户角色是否有权限访问
        Mono<AuthorizationDecision> authorizationDecisionMono = authentication
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(authority -> {
                    return true;
//                    String roleCode = authority.substring(SecurityConstants.AUTHORITY_PREFIX.length()); // 用户的角色
//                    if (GlobalConstants.ROOT_ROLE_CODE.equals(roleCode)) {
//                        return true; // 如果是超级管理员则放行
//                    }
//                    boolean hasAuthorized = CollectionUtil.isNotEmpty(authorizedRoles) && authorizedRoles.contains(roleCode);
//                    return hasAuthorized;
                })
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));
        return authorizationDecisionMono;
//        return Mono.just(new AuthorizationDecision(true));
    }
}
