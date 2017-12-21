package tech.corydaniel.security;

import javax.servlet.http.HttpServletRequest;

public interface JwtHeaderTokenExtractor {
	public String extractAuthHeaderToken(HttpServletRequest request);
}
