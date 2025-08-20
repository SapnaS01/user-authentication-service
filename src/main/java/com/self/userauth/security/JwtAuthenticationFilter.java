package com.self.userauth.security;


import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.self.userauth.model.User;
import com.self.userauth.repository.UserRepository;
import com.self.userauth.service.impl.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Extract the JWT token from the request header
		final String authHeader = request.getHeader("Authorization");
		if (authHeader== null || !authHeader.startsWith("Bearer ")) {
			// If the token is not present or does not start with "Bearer ", continue the filter chain
			filterChain.doFilter(request, response);
			return;
		}
		// Extract the JWT token by removing the "Bearer " prefix		
		final String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
		//		extract the subject (user ID) from the JWT token
		String userId = jwtService.extractSubject(jwtToken);

		// If the user ID is not null, set it in the SecurityContextHolder
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			User user = userRepository.findById(userId)
					.orElse(null);
			if (user != null && jwtService.isTokenValid(jwtToken, user)) {
				// Set the user in the SecurityContextHolder
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());

				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			} 
		}
		// Continue the filter chain
		filterChain.doFilter(request, response);
	}

	/**
	 * Exclude Swagger and public endpoints from JWT authentication filter.
	 * This avoids forcing authentication on Swagger UI & API docs.
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		return path.startsWith("/swagger-ui")
				|| path.startsWith("/swagger-resources")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/webjars");
	}

}
