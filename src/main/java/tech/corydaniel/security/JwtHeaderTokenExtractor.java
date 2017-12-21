package com.deliverable.security;

import javax.servlet.http.HttpServletRequest;

public interface JwtHeaderTokenExtractor {
	public String extractAuthHeaderToken(HttpServletRequest request);
}
