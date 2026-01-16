package com.smartproject.platform.security.jwt;

import com.smartproject.platform.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${app.jwtSecret:SecretKeyGeneratedByAIForSmartProjectPlatformMustBeLongEnoughToSecureTheApplication}")
  private String jwtSecret;

  @Value("${app.jwtExpirationMs:86400000}")
  private int jwtExpirationMs;

  public String generateJwtToken(Authentication authentication) {

    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject((userPrincipal.getUsername()))
        .claim("userId", userPrincipal.getId().toString())
        .claim("email", userPrincipal.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }
  
  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)); // Ensure secret is Base64 encoded if following this pattern, or just bytes. 
    // Actually for simplicity let's use the secret string directly as bytes if it's not base64. 
    // But standard practice is often base64. Let's assume the property provided will be a strong secret string.
    // For this example I'll just use the bytes of the string to avoid decoding errors if user changes it to plain text.
    // return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    // Wait, the decoders.base64.decode is safer if we provide a base64 string. I will provide a base64 string in default.
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key()).build()
               .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    } catch (SecurityException e) {
      logger.error("JWT signature validation failed: {}", e.getMessage());
    }

    return false;
  }
  
  /**
   * Get token expiration date
   */
  public Date getExpirationFromToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
  }
  
  /**
   * Check if token is about to expire (within 5 minutes)
   */
  public boolean isTokenExpiringSoon(String token) {
    try {
      Date expiration = getExpirationFromToken(token);
      long timeToExpire = expiration.getTime() - new Date().getTime();
      return timeToExpire < 300000; // 5 minutes in milliseconds
    } catch (Exception e) {
      return true;
    }
  }
  
  /**
   * Check if token is expired
   */
  public boolean isTokenExpired(String token) {
    try {
      Date expiration = getExpirationFromToken(token);
      return expiration.before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
}
