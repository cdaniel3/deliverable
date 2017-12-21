package tech.corydaniel.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtHeaderTokenExtractorImpl implements JwtHeaderTokenExtractor {
	public static final String AUTHENTICATION_HEADER_NAME = "Authorization";
    public static final String HEADER_PREFIX = "Bearer ";

    public String extractAuthHeaderToken(HttpServletRequest request) {
    	if (request == null) {
    		throw new AuthenticationServiceException("Authorization error");
    	}
    	String header = request.getHeader(AUTHENTICATION_HEADER_NAME);
    	if (StringUtils.isEmpty(header)) {
            throw new AuthenticationServiceException("Authorization header required");
        }

        if (header.length() < HEADER_PREFIX.length()) {
            throw new AuthenticationServiceException("Invalid authorization header size");
        }

        return header.substring(HEADER_PREFIX.length(), header.length());
    }
}

