package com.example.commute.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberResponse {
    private String username; // username으로 명확하게 지정
    private String token;
}
