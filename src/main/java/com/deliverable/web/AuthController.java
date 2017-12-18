package com.deliverable.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.exceptions.JwtServerException;
import com.deliverable.model.User;
import com.deliverable.repositories.UserRepository;
import com.deliverable.security.JwtHeaderTokenExtractor;
import com.deliverable.security.LoginRequest;
import com.deliverable.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@RestController
public class AuthController {

	private Log log = LogFactory.getLog(AuthController.class);

	@Autowired
    private UserRepository userRepository;    
    @Autowired
    private JwtHeaderTokenExtractor tokenExtractor;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository, JwtHeaderTokenExtractor tokenExtractor, JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.tokenExtractor = tokenExtractor;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping(value="/auth/login")
    public @ResponseBody JwtResponse logInAndObtainAccessToken(@RequestBody LoginRequest loginRequest) {
    	if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername()) || StringUtils.isEmpty(loginRequest.getPassword())) {
            throw new AuthenticationServiceException("username and password required");
        }
    	UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
    	Authentication authentication = authenticationManager.authenticate(userPassAuthToken);
    	if (authentication != null && authentication.isAuthenticated()) {
    		String username = (String) authentication.getPrincipal();            
            String accessToken = jwtService.createAccessToken(username, authentication.getAuthorities());
            String refreshToken = jwtService.createRefreshToken(username);
                        
        	return new JwtResponse(accessToken, refreshToken);
    	} else {
    		// Throw (non-specific) error to hide possibly sensitive info
    		throw new AuthenticationServiceException("Authentication error");
    	}    	
    }

    @GetMapping(value="/auth/token", produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody JwtResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	log.trace("refreshToken(HttpServletRequest request, HttpServletResponse response)");
        String token = tokenExtractor.extractAuthHeaderToken(request);
        
        Jws<Claims> claims = jwtService.parseClaims(token);
        if (claims == null) {
        	throw new JwtServerException();
        }
        String subject = claims.getBody().getSubject();
        User user = userRepository.findUserByUsername(subject);
        if (user == null) {
        	throw new UsernameNotFoundException("User not found: " + subject);
        }

        String jwtToken = jwtService.createAccessToken(user.getUsername(), user.getRoles());
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken(jwtToken);
        return jwtResponse;
    }

}
