package com.deliverable.security.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.deliverable.security.JwtHeaderTokenExtractor;
import com.deliverable.security.JwtTokenAuthenticationProcessingFilter;
import com.deliverable.security.LoginAuthenticationProvider;
import com.deliverable.security.LoginProcessingFilter;
import com.deliverable.security.SimpleJwtAuthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private Log log = LogFactory.getLog(WebSecurityConfig.class);
    public static final String AUTHENTICATION_HEADER_NAME = "Authorization";
    public static final String AUTHENTICATION_URL = "/auth/login";
    public static final String REFRESH_TOKEN_URL = "/auth/token";
    public static final String API_ROOT_URL = "/**";

    @Autowired
    private AuthenticationSuccessHandler successHandler;
    @Autowired
    private AuthenticationFailureHandler failureHandler;
    
    @Autowired
    private LoginAuthenticationProvider loginAuthenticationProvider;
    @Autowired
    private SimpleJwtAuthProvider simpleJwtAuthProvider;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtHeaderTokenExtractor tokenExtractor;

    private AntPathRequestMatcher[] requestMatchers = {
    		new AntPathRequestMatcher(AUTHENTICATION_URL),
    		new AntPathRequestMatcher(REFRESH_TOKEN_URL)   		
    };

    private LoginProcessingFilter buildLoginProcessingFilter(String loginEntryPoint) throws Exception {
        LoginProcessingFilter filter = new LoginProcessingFilter(loginEntryPoint, successHandler, failureHandler, objectMapper);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    private JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilter() throws Exception {
        JwtTokenAuthenticationProcessingFilter filter
            = new JwtTokenAuthenticationProcessingFilter(failureHandler, tokenExtractor, (request -> {
            	for (AntPathRequestMatcher rMatcher : requestMatchers) {
            		if (rMatcher.matches(request)) {
            			return false;
            		}
            	}
            	return true;
            }));
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(loginAuthenticationProvider);
        auth.authenticationProvider(simpleJwtAuthProvider);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	log.trace("configure(HttpSecurity http)");
        http
            .csrf().disable() // We don't need CSRF for JWT based authentication
            .exceptionHandling()
//            .authenticationEntryPoint(this.authenticationEntryPoint)

            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
                .authorizeRequests()
                .antMatchers(AUTHENTICATION_URL, REFRESH_TOKEN_URL)
                .permitAll()
            .and()
                .authorizeRequests()
                .antMatchers(API_ROOT_URL).authenticated() // Protected API End-points
            .and()
//                .addFilterBefore(new CustomCorsFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildLoginProcessingFilter(AUTHENTICATION_URL), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
