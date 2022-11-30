package com.mcgoo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * 令牌的配置
 */
@Configuration
public class TokenConfig {

    // todo
    @Value("${mcgoo.signKey:mcgoo}")
    private String signKey;

    /**
     * 令牌的存储策略
     */
    @Bean
    public TokenStore tokenStore() {
        //使用JwtTokenStore生成JWT令牌
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * JwtAccessTokenConverter
     * TokenEnhancer的子类，在JWT编码的令牌值和OAuth身份验证信息之间进行转换。
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 设置秘钥
        converter.setSigningKey(signKey);
        return converter;
    }

    /**
     * JWT令牌增强，继承JwtAccessTokenConverter
     * 将业务所需的额外信息放入令牌中，这样下游微服务就能解析令牌获取
     */
//    public static class JwtAccessTokenEnhancer extends JwtAccessTokenConverter {
//        /**
//         * 重写enhance方法，在其中扩展
//         */
//        @Override
//        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
//            Object principal = authentication.getUserAuthentication().getPrincipal();
//            if (principal instanceof LoginUser){
//                LoginUser user = (LoginUser)principal;
//                LinkedHashMap<String,Object> extendInformation = new LinkedHashMap<>();
//                //设置用户的userId,tenantId
//                extendInformation.put(TokenConstant.USER_ID, user.getUserId());
//                extendInformation.put("tenant_id", user.getTenantId());
//                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(extendInformation);
//            }
//            return super.enhance(accessToken, authentication);
//        }
//    }
}