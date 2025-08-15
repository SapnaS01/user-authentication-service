package com.self.userauth.service.impl;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.self.userauth.config.JwtProperties;
import com.self.userauth.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
	private final JwtProperties jwtProperties;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
	}

	/**
	 * Generate an Access Token
	 */
	public String generateAccessToken(User user) {
		return buildToken(Map.of(), user.getId(), jwtProperties.getAccessExpiration());
	}

	/**
	 * Generate a Refresh Token
	 */
	public String generateRefreshToken(User user) {
		return buildToken(Map.of(), user.getId(), jwtProperties.getRefreshExpiration());
	}

	/**
	 * Build a signed JWT with  claims.
	 */
	private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(subject) // sub
				.setIssuedAt(new Date(System.currentTimeMillis())) // iat
				.setExpiration(new Date(System.currentTimeMillis() + expiration)) // exp
				.setIssuer(jwtProperties.getIssuer()) // iss
				.setAudience(jwtProperties.getAudience()) // aud
				.setId(UUID.randomUUID().toString()) // jti
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	/**
	 * Validate the token against the user and expiration.
	 */
	public boolean isTokenValid(String token, User user) {
		final String subject = extractSubject(token);
		return subject.equals(user.getId().toString()) && !isTokenExpired(token);
	}

	/**
	 * Extract the subject (user ID) from the token.
	 */    
	public String extractSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extract a claim from the token using a claims resolver function.
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Extract all claims from the token.
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.requireIssuer(jwtProperties.getIssuer())
				.requireAudience(jwtProperties.getAudience())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	/**
	 * Extract the expiration date from the token.
	 */
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * 
	 * @param token
	 * @return true if the token is expired, false otherwise	
	 */
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}


}
