package com.deliverable.security;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.deliverable.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@RunWith(MockitoJUnitRunner.class)
public class SimpleJwtAuthProviderTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private Jws<Claims> claims;

	@Mock
	private Claims claimsBody;

	@Mock
	private Authentication auth;

	private SimpleJwtAuthProvider simpleJwtAuthProvider;

	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
		String simpleUserToken = "test token";

		when(auth.getCredentials()).thenReturn(simpleUserToken);
		when(jwtService.parseClaims(simpleUserToken)).thenReturn(claims);

		when(claims.getBody()).thenReturn(claimsBody);				
		when(claimsBody.getSubject()).thenReturn("subj1");

		simpleJwtAuthProvider = new SimpleJwtAuthProvider(jwtService);
	}

	@After
	public void tearDown() {
		simpleJwtAuthProvider = null;
		jwtService = null;
		claims = null;
		claimsBody = null;
		auth = null;
	}

	@Test
	public void testAuthenticateUserNoRoles() {
		when(claimsBody.get("scopes", List.class)).thenReturn(null);
		
		Authentication authToken = simpleJwtAuthProvider.authenticate(auth);
		assertThat("Claims subject doesn't equal subject provided in auth token", authToken.getName(), is("subj1"));
		assertThat("Auth token credentials doesn't equal encoded token", authToken.getCredentials(), is("test token"));
		assertThat("Authorities should be empty for a user with no roles", authToken.getAuthorities(), empty());
	}

	@Test
	public void testAuthenticateUserOneRole() {
		when(claimsBody.get("scopes", List.class)).thenReturn(Arrays.asList("role-user"));
		Authentication authToken = simpleJwtAuthProvider.authenticate(auth);
		assertThat("Claims subject doesn't equal subject provided in auth token", authToken.getName(), is("subj1"));
		assertThat("Auth token credentials doesn't equal encoded token", authToken.getCredentials(), is("test token"));
		Collection<? extends GrantedAuthority> authorities = authToken.getAuthorities();
		assertThat("User with one role should only have 1 item in authorities", authorities, hasSize(1));
		GrantedAuthority firstAuthority = authorities.iterator().next();
		assertThat("User with one role should only have 'role-user' as a role", firstAuthority.getAuthority(), equalTo("role-user"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAuthenticateWithNullParameter() {
		simpleJwtAuthProvider.authenticate(null);
	}

}
