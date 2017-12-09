package com.deliverable.security;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.deliverable.model.User;
import com.deliverable.repositories.UserRepository;

@Component
public class LoginAuthenticationProvider implements AuthenticationProvider {
	
	private static final String GENERIC_AUTH_MSG = "Authentication failed";
	
    private PasswordEncoder encoder;

    private UserRepository userRepository;

    private Log log = LogFactory.getLog(LoginAuthenticationProvider.class);

    @Autowired
    public LoginAuthenticationProvider(PasswordEncoder encoder, UserRepository userRepository) {
		this.encoder = encoder;
		this.userRepository = userRepository;
	}

	@Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	log.trace("authenticate(Authentication authentication()");
        Assert.notNull(authentication, GENERIC_AUTH_MSG);

        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        Assert.notNull(username, GENERIC_AUTH_MSG);
        Assert.notNull(password, GENERIC_AUTH_MSG);
        
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
        	throw new UsernameNotFoundException("User not found: " + username);
        }	
        
        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Authentication failure - bad credentials");
        }
        
        List<GrantedAuthority> authorities = null;
        if (user.getRoles() != null) {
        	authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
        }
        
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}