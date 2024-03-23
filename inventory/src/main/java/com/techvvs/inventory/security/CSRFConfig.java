package com.techvvs.inventory.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class CSRFConfig {

    @Value("${security.jwt.token.secret-key:secret-key}") // todo: change this "secret-key" value to a real hash - see link above
    private String secretKey;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Bean
    @ConditionalOnMissingBean
    public CsrfTokenRepository jwtCsrfTokenRepository() {
        return new JwtTokenFilter(jwtTokenProvider);
    }
}
