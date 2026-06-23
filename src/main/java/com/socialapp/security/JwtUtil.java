package com.socialapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

 @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
 private String secret;

 @Value("${jwt.access-token-expiration}")
 private long accessTokenExpiration;

 @Value("${jwt.refresh-token-expiration}")
 private long refreshTokenExpiration;

 // =============================================
 // Generate Tokens
 // =============================================

 public String generateAccessToken(UserDetails userDetails) {
 return buildToken(new HashMap<>(), userDetails, accessTokenExpiration);
 }

 public String generateRefreshToken(UserDetails userDetails) {
 Map<String, Object> extraClaims = new HashMap<>();
 extraClaims.put("type", "refresh");
 return buildToken(extraClaims, userDetails, refreshTokenExpiration);
 }

 private String buildToken(Map<String, Object> extraClaims,
 UserDetails userDetails,
 long expiration) {
 return Jwts.builder()
 .setClaims(extraClaims)
 .setSubject(userDetails.getUsername())
 .setIssuedAt(new Date(System.currentTimeMillis()))
 .setExpiration(new Date(System.currentTimeMillis() + expiration))
 .signWith(getSigningKey(), SignatureAlgorithm.HS256)
 .compact();
 }

 // =============================================
 // Validate Token
 // =============================================

 public boolean isTokenValid(String token, UserDetails userDetails) {
 try {
 final String username = extractUsername(token);
 if (userDetails == null) {
 // Khi không có UserDetails (vd: để verify refresh token), chỉ cần check expired
 return !isTokenExpired(token);
 }
 return username.equals(userDetails.getUsername())
 && !isTokenExpired(token);
 } catch (Exception e) {
 log.warn("Token validation failed: {}", e.getMessage());
 return false;
 }
 }

 private boolean isTokenExpired(String token) {
 return extractExpiration(token).before(new Date());
 }

 // =============================================
 // Extract Claims
 // =============================================

 public String extractUsername(String token) {
 return extractClaim(token, Claims::getSubject);
 }

 public Date extractExpiration(String token) {
 return extractClaim(token, Claims::getExpiration);
 }

 /**
 * Lấy giá trị của claim "type" — dùng để phân biệt access vs refresh token.
 * Trả về null nếu không có claim.
 */
 public String extractTokenType(String token) {
 Object type = extractClaim(token, claims -> claims.get("type"));
 return type != null ? type.toString() : null;
 }

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

 private Key getSigningKey() {
 byte[] keyBytes = Decoders.BASE64.decode(secret);
 return Keys.hmacShaKeyFor(keyBytes);
 }
}