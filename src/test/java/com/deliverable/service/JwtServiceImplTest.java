package com.deliverable.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.BadCredentialsException;

import com.deliverable.exceptions.JwtExpiredTokenException;
import com.deliverable.security.config.JwtSettings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RunWith(MockitoJUnitRunner.class)
public class JwtServiceImplTest {
	
	private static final Integer TOKEN_EXPIRATION = 15;
	private static final Integer REFRESH_TOKEN_EXPIRATION = 30;
	private static final String TOKEN_ISSUER = "issuer";
	private static final String TOKEN_SIGNING_KEY = "secret";

	private JwtServiceImpl jwtServiceImpl;
	
	private Collection<SimpleGrantedAuthority> simpleUserAuthorities;
	private Collection<SimpleGrantedAuthority> adminUserAuthorities;
	private Collection<SimpleGrantedAuthority> noAuthorities;
	
	@Mock
	private JwtSettings jwtSettings;
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
		setupAuthorityMocks();
		
		when(jwtSettings.getTokenExpirationTime()).thenReturn(TOKEN_EXPIRATION);
		when(jwtSettings.getTokenIssuer()).thenReturn(TOKEN_ISSUER);
		when(jwtSettings.getTokenSigningKey()).thenReturn(TOKEN_SIGNING_KEY);
		when(jwtSettings.getRefreshTokenExpTime()).thenReturn(REFRESH_TOKEN_EXPIRATION);
		
		jwtServiceImpl = new JwtServiceImpl(this.jwtSettings);
	}
	
	private void setupAuthorityMocks() {
		Collection<SimpleGrantedAuthority> simpleUserAuthorities = new ArrayList<SimpleGrantedAuthority>();
		simpleUserAuthorities.add(new SimpleGrantedAuthority("role_user"));
		this.simpleUserAuthorities = simpleUserAuthorities;
		
		Collection<SimpleGrantedAuthority> adminUserAuthorities = new ArrayList<SimpleGrantedAuthority>();
		adminUserAuthorities.add(new SimpleGrantedAuthority("role_user"));
		adminUserAuthorities.add(new SimpleGrantedAuthority("role_admin"));
		this.adminUserAuthorities = adminUserAuthorities;
		
		Collection<SimpleGrantedAuthority> noAuthorities = new ArrayList<SimpleGrantedAuthority>();
		this.noAuthorities = noAuthorities;
	}
	
	@After
	public void tearDown() {
		this.simpleUserAuthorities = null;
		this.adminUserAuthorities = null;
		this.noAuthorities = null;
		jwtServiceImpl = null;
	}
	
	// Access Token testing:

	@Test
	public void testCreateSuccessfulAccessTokenForNonAdminUser() {
		String token = jwtServiceImpl.createAccessToken("user1", this.simpleUserAuthorities);
		Jws<Claims> claims = Jwts.parser().setSigningKey(TOKEN_SIGNING_KEY).parseClaimsJws(token);
		@SuppressWarnings("unchecked")
		List<String> scopes = (List<String>) claims.getBody().get("scopes");
		assertThat("Jwt token created with incorrect subject", claims.getBody().getSubject(), is("user1"));
		assertThat("Jwt token created with incorrect issuer", claims.getBody().getIssuer(), is(TOKEN_ISSUER));
		assertThat("Jwt token doesn't contain expire time", claims.getBody(), IsMapContaining.hasKey("exp"));
		assertThat("Jwt token doesn't contain role_user scope", scopes, IsCollectionContaining.hasItem("role_user"));
		assertThat("Jwt token contains role_admin scope, but shouldn't", scopes, IsNot.not(IsCollectionContaining.hasItem("role_admin")));
	}
	
	@Test
	public void testCreateSuccessfulAccessTokenForAdminUser() {
		String token = jwtServiceImpl.createAccessToken("admin", this.adminUserAuthorities);
		Jws<Claims> claims = Jwts.parser().setSigningKey(TOKEN_SIGNING_KEY).parseClaimsJws(token);
		@SuppressWarnings("unchecked")
		List<String> scopes = (List<String>) claims.getBody().get("scopes");
		assertThat("Jwt token created with incorrect subject", claims.getBody().getSubject(), is("admin"));
		assertThat("Jwt token doesn't contain role_user scope", scopes, IsCollectionContaining.hasItem("role_user"));
		assertThat("Jwt token doesn't contain role_admin scope", scopes, IsCollectionContaining.hasItem("role_admin"));
	}
	
	@Test
	public void testCreateSuccessfulAccessTokenForUserWithEmptyRoles() {
		String token = jwtServiceImpl.createAccessToken("emptyRolesUser", this.noAuthorities);
		Jws<Claims> claims = Jwts.parser().setSigningKey(TOKEN_SIGNING_KEY).parseClaimsJws(token);
		assertThat("Jwt token created with incorrect subject", claims.getBody().getSubject(), is("emptyRolesUser"));
		assertThat("Jwt token contains scopes, but should be empty", claims.getBody(), IsNot.not(IsMapContaining.hasKey("scope")));
	}

	@Test
	public void testCreateSuccessfulAccessTokenForUserWithNoRoles() {
		String token = jwtServiceImpl.createAccessToken("noRoleUser");
		Jws<Claims> claims = Jwts.parser().setSigningKey(TOKEN_SIGNING_KEY).parseClaimsJws(token);
		assertThat("Jwt token created with incorrect subject", claims.getBody().getSubject(), is("noRoleUser"));
		assertThat("Jwt token contains scopes, but should be empty", claims.getBody(), IsNot.not(IsMapContaining.hasKey("scope")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateAccessTokenEmptyUsername() {
		jwtServiceImpl.createAccessToken("", this.noAuthorities);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateAccessTokenNullUsername() {
		jwtServiceImpl.createAccessToken(null);
	}

	// Refresh Token testing:

	@Test
	public void testCreateSuccessfulRefreshTokenForUser() {
		String token = jwtServiceImpl.createRefreshToken("refreshUser");
		Jws<Claims> claims = Jwts.parser().setSigningKey(TOKEN_SIGNING_KEY).parseClaimsJws(token);
		@SuppressWarnings("unchecked")
		List<String> scopes = (List<String>) claims.getBody().get("scopes");
		Date expr = claims.getBody().getExpiration();
		assertThat("Jwt token created with incorrect subject", claims.getBody().getSubject(), is("refreshUser"));
		assertThat("Jwt token doesn't contain refresh scope", scopes, IsCollectionContaining.hasItem("ROLE_REFRESH_TOKEN"));
		assertTrue("Expiration date is in the past", expr.after(new Date()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateRefreshTokenEmptyUsername() {
		jwtServiceImpl.createRefreshToken("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateRefreshTokenNullUsername() {
		jwtServiceImpl.createRefreshToken(null);
	}

	// Parse claims testing:

	@Test
	public void testParseClaimsValidToken() {
		Jws<Claims> claims = jwtServiceImpl.parseClaims(createToken("testUser", 30));
		assertThat("Parsed claims contains incorrect subject", claims.getBody().getSubject(), is("testUser"));
	}

	@Test(expected=BadCredentialsException.class)
	public void testParseClaimsInvalidToken() {
		String validToken = createToken("testUser", 30);
		String invalidToken = validToken.substring(0, validToken.length()-1);
		jwtServiceImpl.parseClaims(invalidToken);
	}

	@Test(expected=JwtExpiredTokenException.class)
	public void testParseClaimsExpiredToken() {
		String expiredToken = createToken("testUser", -30);
		jwtServiceImpl.parseClaims(expiredToken);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testParseClaimsNullToken() {
		jwtServiceImpl.parseClaims(null);
	}

	private String createToken(String subject, int expireMinutes) {
		Claims claims = Jwts.claims().setSubject(subject);
		Calendar expireCal = Calendar.getInstance();
	    expireCal.add(Calendar.MINUTE, expireMinutes);

	    return Jwts.builder()
	      .setClaims(claims)
	      .setIssuer("testIssuer")
	      .setExpiration(expireCal.getTime())
	      .signWith(SignatureAlgorithm.HS512, TOKEN_SIGNING_KEY)
	    .compact();
	}

}
