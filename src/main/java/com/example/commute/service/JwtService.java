package com.example.commute.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 토큰에서 사용자 이름 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Claims에서 특정 값을 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 기본적으로 extraClaims 없이 토큰을 생성
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // JWT 토큰 생성
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("roles", userDetails.getAuthorities());

        // SecretKey를 생성하는 방법 (비밀 키를 직접 지정할 수 있음)
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());  // secret은 String으로 사용

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 서명 방식 변경
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 토큰 만료 여부 체크
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 토큰에서 만료 날짜 추출
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 토큰에서 Claims 추출
    private Claims extractAllClaims(String token) {
        // 최신 버전에서는 parserBuilder를 사용하여 SecretKey로 검증해야 함
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder() // parserBuilder 사용
                .setSigningKey(secretKey) // SecretKey 사용
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰의 유효성 검사 (기본적으로 단순히 검증만)
    public boolean validateToken(String token) {
        try {
            // 최신 버전에서는 parserBuilder를 사용하여 SecretKey로 검증
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
