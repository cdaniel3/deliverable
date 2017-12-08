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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.User;
import com.deliverable.repositories.UserRepository;
import com.deliverable.security.JwtHeaderTokenExtractor;
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
        List<String> roles = user.getRoles();
        if (roles != null) {
        	authorities = roles.stream()
        			.map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
        }
        String jwtToken = jwtService.createAccessToken(user.getUsername(), authorities);        
        log.debug("jwtToken: " + jwtToken);
        Map<String, String> tokenMap = new HashMap<String,String>(1);
        tokenMap.put("token", jwtToken);
        return tokenMap;
    }
}
