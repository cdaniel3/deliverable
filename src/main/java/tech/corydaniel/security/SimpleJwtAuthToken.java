package com.deliverable.security;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class SimpleJwtAuthToken extends AbstractAuthenticationToken {

	private Log log = LogFactory.getLog(SimpleJwtAuthToken.class);
	private static final long serialVersionUID = -7902386557142643055L;
	private String encodedToken;
    private String username;

    public SimpleJwtAuthToken(String encodedToken) {
        super(null);
        log.trace("SimpleJwtAuthToken(String encodedToken) constructor");
        this.encodedToken = encodedToken;
        this.setAuthenticated(false);
    }

    public SimpleJwtAuthToken(String username, String encodedToken, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        log.trace("SimpleJwtAuthToken(String username, String encodedToken, Collection<? extends GrantedAuthority> authorities) constructor");
        this.username = username;
        this.encodedToken = encodedToken;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return encodedToken;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

}

