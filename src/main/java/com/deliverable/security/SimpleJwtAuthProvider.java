package com.deliverable.security;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.deliverable.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@Component
public class SimpleJwtAuthProvider implements AuthenticationProvider {
	
	@Autowired
	private JwtService jwtService;
    private Log log = LogFactory.getLog(SimpleJwtAuthProvider.class);
        
    @SuppressWarnings("unchecked")
	@Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	log.trace("SimpleJwtAuthProvider authenticate(Authentication authentication)");
        String token = (String) authentication.getCredentials();
        Jws<Claims> jwsClaims = jwtService.parseClaims(token);
        
        String subject = jwsClaims.getBody().getSubject();        
        List<String> scopes = jwsClaims.getBody().get("scopes", List.class);
        List<GrantedAuthority> authorities = scopes.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
                
        return new SimpleJwtAuthToken(subject, token, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (SimpleJwtAuthToken.class.isAssignableFrom(authentication));
    }
}

