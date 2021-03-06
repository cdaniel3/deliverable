package tech.corydaniel.security;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import tech.corydaniel.model.Role;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class LoginAuthenticationProviderTest {
	
	private LoginAuthenticationProvider loginAuthenticationProvider;
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	private User validUserEntity;
	private User validAdminUserEntity;
	private User validUserInvalidRolesEntity;
	
	@Mock
	private UserRepository userRepo;
	@Mock
	private Authentication validAuthentication;
	@Mock
	private Authentication validUserInvalidRolesAuthentication;
	@Mock
	private Authentication validAdminAuthentication;
	@Mock
	private Authentication invalidAuthentication;
	@Mock
	private Authentication noUserAuthentication;
	@Mock
	private Authentication badAuthObjectAuthentication;
	
	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
		setupUsers();
		
		when(validAuthentication.getPrincipal()).thenReturn("user1");
		when(validAdminAuthentication.getPrincipal()).thenReturn("admin");
		when(invalidAuthentication.getPrincipal()).thenReturn("user1");
		when(noUserAuthentication.getPrincipal()).thenReturn("user-doesnt-exist");
		when(badAuthObjectAuthentication.getPrincipal()).thenReturn("user1");
		when(validUserInvalidRolesAuthentication.getPrincipal()).thenReturn("userInvalidRoles");
		
		when(validAuthentication.getCredentials()).thenReturn("password");
		when(validAdminAuthentication.getCredentials()).thenReturn("adminPassword");
		when(invalidAuthentication.getCredentials()).thenReturn("incorrectPassword");
		when(noUserAuthentication.getCredentials()).thenReturn("password");
		when(badAuthObjectAuthentication.getCredentials()).thenReturn(null);
		when(validUserInvalidRolesAuthentication.getCredentials()).thenReturn("password");
		
		when(userRepo.findUserByUsername("user1")).thenReturn(validUserEntity);
		when(userRepo.findUserByUsername("admin")).thenReturn(validAdminUserEntity);
		when(userRepo.findUserByUsername("user-doesnt-exist")).thenReturn(null);
		when(userRepo.findUserByUsername("userInvalidRoles")).thenReturn(validUserInvalidRolesEntity);
		
		loginAuthenticationProvider = new LoginAuthenticationProvider(this.encoder, this.userRepo);
	}
	
	public void setupUsers() {
		User user = new User();
		user.setUsername("user1");
		user.setPassword(encoder.encode("password"));
		this.validUserEntity = user;
		
		User adminUser = new User();
		adminUser.setUsername("admin");
		adminUser.setPassword(encoder.encode("adminPassword"));
		adminUser.setRoles(Arrays.asList(new Role[] { new Role("admin") }));
		this.validAdminUserEntity = adminUser;
		
		User invalidRoles = new User();
		invalidRoles.setUsername("userInvalidRoles");
		invalidRoles.setPassword(encoder.encode("password"));
		invalidRoles.setRoles(Arrays.asList(new Role[] { new Role("user"), null, new Role("modifier") }));
		this.validUserInvalidRolesEntity = invalidRoles;
	}
	
	@Test
	public void testAuthenticateSuccessfulValidUser() {
		Authentication auth = loginAuthenticationProvider.authenticate(this.validAuthentication);
		assertThat("User should be authenticated", auth.isAuthenticated(), is(true));
		assertThat("Authorities should be null for user with no roles", auth.getAuthorities(), anyOf(IsEmptyCollection.empty(), nullValue()));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAuthenticationValidUserInvalidRoles() {
		// Throws IllegalArgumentException when attempting to insert null Role into Authorities
		loginAuthenticationProvider.authenticate(this.validUserInvalidRolesAuthentication);
	}
	
	@Test
	public void testAuthenticateSuccessfulValidAdminUser() {
		Authentication auth = loginAuthenticationProvider.authenticate(this.validAdminAuthentication);
		assertThat("Admin user should be authenticated", auth.isAuthenticated(), is(true));
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		assertThat("Admin user should have an 'admin' role", authorities.stream().anyMatch(ga -> ga.getAuthority().equals("admin")),
				is(true));	
	}
	
	@Test(expected=BadCredentialsException.class)
	public void testAuthenticationFailureInvalidPassword() {
		Authentication auth = loginAuthenticationProvider.authenticate(this.invalidAuthentication);
		assertThat("User should NOT be authenticated using invalid password", auth.isAuthenticated(), is(false));
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testUserNotFound() {
		loginAuthenticationProvider.authenticate(this.noUserAuthentication);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullAuth() {
		loginAuthenticationProvider.authenticate(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBadAuthObject() {
		loginAuthenticationProvider.authenticate(this.badAuthObjectAuthentication);
	}
	
}
