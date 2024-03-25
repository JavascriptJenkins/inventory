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

    // Entry points
    // ANYTHING AUTH NEEDS TO BE DECLARED HERE
    http
      // Apply JWT
            .apply(new JwtTokenFilterConfigurer(jwtTokenProvider))
                            .and()


            .csrf()

//            .csrfTokenRepository(jwtCsrfTokenRepository)
            .and()

            .authorizeRequests()//
        .antMatchers("/login").permitAll()//
        .antMatchers("/css/table.css").permitAll()//
        .antMatchers("/dashboard/index/*").permitAll()//
        .antMatchers("/login/logout").permitAll()//
             .antMatchers("/customer/pipeline").permitAll()
//        .antMatchers("/file/download/**").permitAll() // note: will be checking jwt for download on the controller
//        .antMatchers("/file/download/*").permitAll() // note: will be checking jwt for download on the controller
//        .antMatchers("/file/download").permitAll() // note: will be checking jwt for download on the controller
//        .antMatchers("/file/upload/**").permitAll() //note: will be checking jwt for download on the controller
//        .antMatchers("/file/upload/*").permitAll() //note: will be checking jwt for download on the controller
//        .antMatchers("/file/upload").permitAll() //note: will be checking jwt for download on the controller
        .antMatchers("/login/systemuser").permitAll()//
        .antMatchers("/login/createSystemUser").permitAll()//
        .antMatchers("/testdata/makedev").permitAll()//
        .antMatchers("/login/resetpassword").permitAll()//
         .antMatchers("/customer/pipeline").permitAll()//
        .antMatchers("/login/viewresetpass").permitAll()//
        .antMatchers("/login/resetpasswordbyemail").permitAll()//
        .antMatchers("/login/actuallyresetpassword").permitAll()//
        .antMatchers("/login/verifyphonetoken").permitAll()//
        .antMatchers("/newbatch/editform/**").permitAll()//
        .antMatchers("/login/verify/**").permitAll()//
        .antMatchers("/newbatch/createNewBatch/*").permitAll()//
        .antMatchers("/newbatch/searchBatch/*").permitAll()//
        .antMatchers("/newbatch/browseBatch/***").permitAll()//
        .antMatchers("/newbatch/editBatch/*").permitAll()//
        .antMatchers("/login/createaccount").permitAll()//
        .antMatchers("/users/sendResetToken").permitAll()//
        .antMatchers("/users/validateUserEmail").permitAll()//
        .antMatchers("/paypal/paypal").permitAll()//
        .antMatchers("/paypal/*").permitAll()//
        .antMatchers("/h2-console/**/**").permitAll()


        // Disallow everything else..
        .anyRequest().authenticated()



    //.cors().configurationSource(corsConfigurationSource())
    ;

    // If a user try to access a resource without having enough permissions
    http.exceptionHandling().accessDeniedPage("/login");

    // Apply JWT
//    http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));

    // Optional, if you want to test the API from a browser
    // http.httpBasic();
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
