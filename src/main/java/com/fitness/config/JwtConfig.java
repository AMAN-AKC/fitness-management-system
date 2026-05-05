package com.fitness.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtConfig {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration.ms:3600000}")
	private long expirationMs;

	// ── Extract username (subject) from token ──────────────────────────────
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	// ── Extract expiry date ────────────────────────────────────────────────
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	// ── Extract any claim via function ─────────────────────────────────────
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	// ── Check expiry ───────────────────────────────────────────────────────
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	// ── Generate token for user ────────────────────────────────────────────
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		// Store role in claims for convenience
		claims.put("roles", userDetails.getAuthorities());
		return buildToken(claims, userDetails.getUsername());
	}

	private String buildToken(Map<String, Object> extraClaims, String subject) {
		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	// ── Validate token against UserDetails ────────────────────────────────
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	// ── Build signing key from Base64-encoded secret ───────────────────────
	private Key getSigningKey() {
		byte[] keyBytes = Base64.getDecoder().decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}