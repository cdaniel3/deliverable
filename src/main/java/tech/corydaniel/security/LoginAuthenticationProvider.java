package tech.corydaniel.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tech.corydaniel.model.User;
import tech.corydaniel.repositories.UserRepository;

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

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	log.trace("authenticate(Authentication authentication()");
    	if (authentication == null) {
    		throw new IllegalArgumentException(GENERIC_AUTH_MSG);
    	}

        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) { 
        	throw new IllegalArgumentException(GENERIC_AUTH_MSG);
        }
                
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
        	throw new UsernameNotFoundException("Authentication failure");
        }	
        
        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Authentication failure");
        }
        
        return new UsernamePasswordAuthenticationToken(username, null, user.getRoles());
    }

    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
