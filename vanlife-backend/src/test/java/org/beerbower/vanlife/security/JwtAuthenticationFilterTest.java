package org.beerbower.vanlife.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.beerbower.vanlife.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        // Mock PrintWriter for response
        responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void shouldContinueFilterChain_WhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldContinueFilterChain_WhenInvalidAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldAuthenticateUser_WhenValidJwtToken() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        Date now = new Date();
        TokenClaims claims = new TokenClaims("testUser", new Date(now.getTime() + 3600000), now, "testIssuer", "ROLE_USER,ROLE_ADMIN");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.validateToken(jwt)).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateToken(jwt);
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("testUser", authentication.getName());
    }

    @Test
    void shouldReturnUnauthorized_WhenInvalidJwtToken() throws ServletException, IOException {
        String jwt = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.validateToken(jwt)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateToken(jwt);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Flush and assert the written response
        responseWriter.flush();
        assertTrue(responseWriter.toString().contains("Invalid JWT token"));
    }
}
