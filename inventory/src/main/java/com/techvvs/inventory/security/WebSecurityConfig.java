package com.techvvs.inventory.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

    @Autowired
    CsrfTokenRepository jwtCsrfTokenRepository;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // Disable CSRF (cross site request forgery)
    http.csrf().disable();

    // No session will be created or used by spring security
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    // Apply JWT
    http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));

    // Entry points
    // ANYTHING AUTH NEEDS TO BE DECLARED HERE
    // Entry points for routes that don't require authentication
    http.authorizeRequests()
            .antMatchers("/login").permitAll()
            .antMatchers("/favicon.ico").permitAll()
            .antMatchers("/css/table.css").permitAll()
            .antMatchers("/login/systemuser").permitAll()//
            .antMatchers("/login/verifyphonetoken").permitAll()//
            .antMatchers("/login/createaccount").permitAll()//
            .antMatchers("/login/createSystemUser").permitAll()//
//            .antMatchers("/login/systemuser").permitAll()//
            // Add other routes that should be publicly accessible
            // Disallow everything else..
            .anyRequest().authenticated();

    // If a user tries to access a resource without having enough permissions
    http.exceptionHandling().accessDeniedPage("/login");
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // Allow swagger to be accessed without authentication
    web.ignoring().antMatchers("/v2/api-docs")//
        .antMatchers("/swagger-resources/**")//
        .antMatchers("/swagger-ui.html")//
        .antMatchers("/configuration/**")//
        .antMatchers("/webjars/**")//
        .antMatchers("/public")
        
        // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
        .and()
        .ignoring()
            .antMatchers("/customer/pipeline")
      //  .antMatchers("/customer/pipeline/*")
        .antMatchers("/h2-console/**/**");;

  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  // this is a breaking change from spring 2.0
  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/customer/pipeline").allowedOrigins("https://techvvs.io");
        registry.addMapping("/customer/pipeline").allowedOrigins("https://techvvs.io:3000");
        registry.addMapping("/customer/pipeline").allowedOrigins("http://techvvs:3000");
      }
    };
  }



}
