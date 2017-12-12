package com.deliverable.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.deliverable.exceptions.JwtExpiredTokenException;
import com.deliverable.security.config.JwtSettings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class JwtServiceImpl implements JwtService {
	
	private final static String REFRESH_TOKEN_AUTHORITY = "ROLE_REFRESH_TOKEN";
	private Log log = LogFactory.getLog(JwtServiceImpl.class);

	private JwtSettings settings;
	
	@Autowired
	public JwtServiceImpl(JwtSettings jwtSettings) {
		this.settings = jwtSettings;
	}
	
	@Override
	public String createAccessToken(String username) {
		return createAccessToken(username, null);
	}

	@Override
	public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
		if (StringUtils.isEmpty(username)) { 
    		throw new IllegalArgumentException("Cannot create JWT Token without username");
		}

	    Claims claims = Jwts.claims().setSubject(username);
	    if (authorities != null) {
	    	claims.put("scopes", authorities.stream().map(s -> s.toString()).collect(Collectors.toList()));
	    }
	    Calendar expireCal = Calendar.getInstance();
	    expireCal.add(Calendar.MINUTE, settings.getTokenExpirationTime());
	    
	    return Jwts.builder()
	      .setClaims(claims)
	      .setIssuer(settings.getTokenIssuer())
	      .setExpiration(expireCal.getTime())
	      .signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey())
	    .compact();
	}

	@Override
	public String createRefreshToken(String username) {
		if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Cannot create JWT Token without username");
        }

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("scopes", Arrays.asList(REFRESH_TOKEN_AUTHORITY));
        
        Calendar expireCal = Calendar.getInstance();
	    expireCal.add(Calendar.MINUTE, settings.getRefreshTokenExpTime());
	    
        return Jwts.builder()
          .setClaims(claims)
          .setIssuer(settings.getTokenIssuer())
          .setId(UUID.randomUUID().toString())
          .setExpiration(expireCal.getTime())
          .signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey())
        .compact();
	}

	@Override
	public Jws<Claims> parseClaims(String token) {
		try {
            return Jwts.parser().setSigningKey(settings.getTokenSigningKey()).parseClaimsJws(token);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
            log.info("Invalid JWT Token: " + ex.getMessage());
            throw new BadCredentialsException("Invalid JWT token: ", ex);
        } catch (ExpiredJwtException expiredEx) {
            log.info("JWT Token is expired: " + expiredEx);
            throw new JwtExpiredTokenException("JWT Token expired", expiredEx);
        }
	}

}

