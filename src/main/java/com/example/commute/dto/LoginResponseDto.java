package com.example.commute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String username;    // 사용자 이름
    private String token;       // JWT 토큰



}
