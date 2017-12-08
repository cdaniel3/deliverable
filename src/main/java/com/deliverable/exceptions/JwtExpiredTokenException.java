package com.deliverable.exceptions;

import org.springframework.security.core.AuthenticationException;

public class JwtExpiredTokenException extends AuthenticationException {
	private static final long serialVersionUID = -107097625979020710L;

	public JwtExpiredTokenException(String msg, Throwable t) {
        super(msg, t);
    }
}
