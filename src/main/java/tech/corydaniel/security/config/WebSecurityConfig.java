package tech.corydaniel.security.config;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tech.corydaniel.security.JwtHeaderTokenExtractor;
import tech.corydaniel.security.JwtTokenAuthenticationProcessingFilter;
import tech.corydaniel.security.LoginAuthenticationProvider;
import tech.corydaniel.security.SimpleJwtAuthProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private Log log = LogFactory.getLog(WebSecurityConfig.class);
    public static final String AUTHENTICATION_URL = "/auth/login";
    public static final String REFRESH_TOKEN_URL = "/auth/token";
    public static final String API_ROOT_URL = "/**";
    
    @Value("${allowed.origins}")
	private String[] allowedOrigins;

    @Autowired
    private AuthenticationFailureHandler failureHandler;
    
    @Autowired
    private LoginAuthenticationProvider loginAuthenticationProvider;
    @Autowired
    private SimpleJwtAuthProvider simpleJwtAuthProvider;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtHeaderTokenExtractor tokenExtractor;

    private AntPathRequestMatcher[] requestMatchers = {
    		new AntPathRequestMatcher(AUTHENTICATION_URL),
    		new AntPathRequestMatcher(REFRESH_TOKEN_URL)   		
    };

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
        	.cors()
			.and()
            .csrf().disable()			// Disabling CSRF since Jwt is used for user operations
            .sessionManagement()
            	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(AUTHENTICATION_URL, REFRESH_TOKEN_URL).permitAll()
                .antMatchers(API_ROOT_URL).authenticated()
            .and()
                .addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration configuration = new CorsConfiguration();
		configuration.applyPermitDefaultValues();
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
    }
}
