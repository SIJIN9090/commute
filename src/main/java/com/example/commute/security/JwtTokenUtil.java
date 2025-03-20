package com.example.commute.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtTokenUtil {

    @Value("${jwt.secret}")  // ✅ application.properties에서 값을 가져옴
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String SECRET_KEY = "secretKey"; // 비밀키

    // JWT 생성 메서드
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // "ROLE_ADMIN" 또는 "ROLE_USER" 형태로 저장
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}