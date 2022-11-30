package com.mcgoo.gateway.config;

import com.mcgoo.common.core.result.ResultCode;
import com.mcgoo.gateway.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@EnableWebFluxSecurity
public class ResourceServerConfig {

    @Autowired
    private ResourceServerManager resourceServerManager;
    @Autowired
    private TokenStore tokenStore;

    @Bean
    SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception{
        //认证过滤器，放入认证管理器tokenAuthenticationManager
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
        http.authenticationManager(reactiveAuthenticationManager())
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/auth/oauth/**","/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/*/v3/api-docs",
                        "/*/v3/api-docs/swagger-config",
                        "/webjars/**",
                        "/doc.html")
                .permitAll()
                .anyExchange()
                .access(resourceServerManager)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler()) // 处理未授权
                .authenticationEntryPoint(authenticationEntryPoint()) //处理未认证
//                .and().csrf().disable()
                .and()
                // 跨域过滤器
//                .addFilterAt(corsFilter, SecurityWebFiltersOrder.CORS)
//                token的认证过滤器，用于校验token和认证
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return (authentication) -> {
            Mono<Authentication> result = Mono.justOrEmpty(authentication)
                    .filter(a -> a instanceof BearerTokenAuthenticationToken)
                    .cast(BearerTokenAuthenticationToken.class)
                    .map(BearerTokenAuthenticationToken::getToken)
                    .flatMap((accessToken -> {
                        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(accessToken);
                        //根据access_token从数据库获取不到OAuth2AccessToken
                        if (oAuth2AccessToken == null) {
                            return Mono.error(new InvalidTokenException("无效的token！"));
                        } else if (oAuth2AccessToken.isExpired()) {
                            return Mono.error(new InvalidTokenException("token已过期！"));
                        }
                        OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
                        if (oAuth2Authentication == null) {
                            return Mono.error(new InvalidTokenException("无效的token！"));
                        } else {
                            return Mono.just(oAuth2Authentication);
                        }
                    })).cast(Authentication.class);
            return result;
        };
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse()))
                    .flatMap(response -> ResponseUtils.writeErrorInfo(response, ResultCode.UNAUTHORIZED));
            return mono;
        };
    }

    /**
     * token无效或者已过期自定义响应
     */
    @Bean
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, e) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse()))
                    .flatMap(response -> ResponseUtils.writeErrorInfo(response, ResultCode.INVALID_TOKEN));
            return mono;
        };
    }
}
