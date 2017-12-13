package com.deliverable.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.Role;
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

    @PostMapping(value="/auth/login")
    public @ResponseBody Map<String,String> logInAndObtainAccessToken(@RequestBody LoginRequest loginRequest) {
    	if (StringUtils.isEmpty(loginRequest) || StringUtils.isEmpty(loginRequest.getUsername()) || StringUtils.isEmpty(loginRequest.getPassword())) {
            throw new AuthenticationServiceException("username and password required");
        }
    	UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
    	Authentication authentication = authenticationManager.authenticate(userPassAuthToken);
    	if (authentication != null && authentication.isAuthenticated()) {
    		String username = (String) authentication.getPrincipal();            
            String accessToken = jwtService.createAccessToken(username, authentication.getAuthorities());
            String refreshToken = jwtService.createRefreshToken(username);
            
            Map<String, String> responseMap = new HashMap<String, String>();   
        	responseMap.put("token", accessToken);
        	responseMap.put("refreshToken", refreshToken);
        	return responseMap;
    	} else {
    		// Throw (non-specific) error to hide possibly sensitive info
    		throw new AuthenticationServiceException("Authentication error");
    	}    	
    }

    @GetMapping(value="/auth/token", produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody Map<String,String> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	log.trace("refreshToken(HttpServletRequest request, HttpServletResponse response)");
        String token = tokenExtractor.extractAuthHeaderToken(request);
        
        Jws<Claims> claims = jwtService.parseClaims(token);
        String subject = claims.getBody().getSubject();
        User user = userRepository.findUserByUsername(subject);
        if (user == null) {
        	throw new UsernameNotFoundException("User not found: " + subject);
        }

        List<GrantedAuthority> authorities = null;
        List<Role> roles = user.getRoles();
        if (roles != null) {
        	authorities = roles.stream()
        			.map(role -> {
        				if (role != null) {
                    		return new SimpleGrantedAuthority(role.getRoleName());
                    	}
                    	return null;        				
        			}).collect(Collectors.toList());
        			
        }
        String jwtToken = jwtService.createAccessToken(user.getUsername(), authorities);        
        log.debug("jwtToken: " + jwtToken);
        Map<String, String> tokenMap = new HashMap<String,String>(1);
        tokenMap.put("token", jwtToken);
        return tokenMap;
    }

}
