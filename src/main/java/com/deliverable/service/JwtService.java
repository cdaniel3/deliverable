package com.deliverable.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtService {
	public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities);
	public String createRefreshToken(String username);
	public Jws<Claims> parseClaims(String token);
}
