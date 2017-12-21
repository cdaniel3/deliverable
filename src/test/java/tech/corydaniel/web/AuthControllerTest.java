package tech.corydaniel.web;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import tech.corydaniel.exceptions.JwtServerException;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.UserRepository;
import tech.corydaniel.security.JwtHeaderTokenExtractor;
import tech.corydaniel.security.LoginRequest;
import tech.corydaniel.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {
	
	private static final String MOCK_ACCESS_TOKEN = "--mock access token--";
	private static final String MOCK_REFRESH_TOKEN = "--mock refresh token--";

	private AuthController authController;
	
	@Mock
	private UserRepository userRepository;
	@Mock
	private JwtHeaderTokenExtractor tokenExtractor;
	@Mock
	private JwtService jwtService;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletRequest successfulRequest;
	@Mock
	private HttpServletRequest badRequest;
	@Mock
	private HttpServletRequest userNotFoundRequest;
	@Mock
	private HttpServletResponse response;
	@Mock
	private Jws<Claims> validClaims;
	@Mock
	private Jws<Claims> userNotFoundClaims;
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
		
		mockAuthenticationManager();
		
		// Access tokens always return "valid" tokens
		when(jwtService.createAccessToken(any(), any())).thenReturn(MOCK_ACCESS_TOKEN);
		when(jwtService.createRefreshToken(any())).thenReturn(MOCK_REFRESH_TOKEN);
		
		// Happy path
		setupSuccessfulMocks();
		
		// Negative cases
		setupUnsuccessfulMocks();
		
		authController = new AuthController(userRepository, tokenExtractor, jwtService, authenticationManager);
	}
	
	private void mockAuthenticationManager() {
		when(authenticationManager.authenticate(any())).thenAnswer(invocation -> {
			UsernamePasswordAuthenticationToken userPassAuthToken = invocation.getArgument(0);
			
			Authentication mockAuthentication = Mockito.mock(Authentication.class);
			when(mockAuthentication.getPrincipal()).thenReturn(userPassAuthToken.getPrincipal());
			if (userPassAuthToken.getPrincipal().equals("validUser") && userPassAuthToken.getCredentials().equals("validPassword")) {
				// valid auth
				when(mockAuthentication.isAuthenticated()).thenReturn(true);
			}  // else isAuthenticated() returns false
			return mockAuthentication;
		});
	}

	private void setupSuccessfulMocks() {
		when(tokenExtractor.extractAuthHeaderToken(successfulRequest)).thenReturn("successfulToken");
		when(jwtService.parseClaims("successfulToken")).thenReturn(validClaims);
		Claims body = Mockito.mock(Claims.class);
		when(body.getSubject()).thenReturn("validUser");
		when(validClaims.getBody()).thenReturn(body);
		User mockUser = Mockito.mock(User.class);
		when(userRepository.findUserByUsername("validUser")).thenReturn(mockUser);
	}
	
	private void setupUnsuccessfulMocks() {
		when(tokenExtractor.extractAuthHeaderToken(badRequest)).thenReturn("badToken");
		// Parse claims will return null in this case without mocking
		// when(jwtService.parseClaims("badToken")).thenReturn(null);
		
		when(tokenExtractor.extractAuthHeaderToken(userNotFoundRequest)).thenReturn("badUserToken");
		when(jwtService.parseClaims("badUserToken")).thenReturn(userNotFoundClaims);
		when(userNotFoundClaims.getBody()).thenReturn(Mockito.mock(Claims.class));		// user.getBody().getSubject() will return null without mocking
	}
	
	@After
	public void tearDown() {
		authController = null;
		userRepository = null;
		tokenExtractor = null;
		jwtService = null;
		authenticationManager = null;
		request = null;
		successfulRequest = null;
		badRequest = null;
		userNotFoundRequest = null;
		response = null;
		validClaims = null;		
		userNotFoundClaims = null;
	}
	
	@Test
	public void testLogInAndObtainAccessTokenSuccess() {
		LoginRequest loginRequest = new LoginRequest("validUser", "validPassword");
		JwtResponse jwtResponse = authController.logInAndObtainAccessToken(loginRequest);
		assertThat("Valid access token not returned", jwtResponse.getAccessToken(), equalTo(MOCK_ACCESS_TOKEN));
		assertThat("Valid refresh token not returned", jwtResponse.getRefreshToken(), equalTo(MOCK_REFRESH_TOKEN));
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void testLogInAndObtainAccessTokenNullLoginRequest() {
		authController.logInAndObtainAccessToken(null);
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void testLogInAndObtainAccessTokenNullUsername() {
		LoginRequest loginRequest = new LoginRequest(null, "password");
		authController.logInAndObtainAccessToken(loginRequest);
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void testLogInAndObtainAccessTokenNullPassword() {
		LoginRequest loginRequest = new LoginRequest("user1", null);
		authController.logInAndObtainAccessToken(loginRequest);
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void testLogInAndObtainAccessTokenBadAuthentication() { 
		LoginRequest loginRequest = new LoginRequest("invalidUser", "invalidPassword");
		authController.logInAndObtainAccessToken(loginRequest);
	}
	
	@Test(expected=JwtServerException.class)
	public void testRefreshTokenNullClaims() throws ServletException, IOException {		
		authController.refreshToken(request, response);
	}
	
	@Test(expected=JwtServerException.class)
	public void testRefreshTokenNullClaimsBetterTest() throws ServletException, IOException {		
		authController.refreshToken(badRequest, response);
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testRefreshTokenUsernameNotFound() throws ServletException, IOException {		
		authController.refreshToken(userNotFoundRequest, response);
	}
	
	@Test
	public void testRefreshTokenSuccess() throws ServletException, IOException {
		JwtResponse jwtResponse = authController.refreshToken(successfulRequest, response);
		assertThat("Valid access token not returned", jwtResponse.getAccessToken(), equalTo(MOCK_ACCESS_TOKEN));
		assertThat("Valid refresh token not returned", jwtResponse.getRefreshToken(), equalTo(MOCK_REFRESH_TOKEN));
	}
	
}
