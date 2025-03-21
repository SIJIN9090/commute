package com.example.commute.controller;

import com.example.commute.dto.LoginRequestDto;

import com.example.commute.dto.MemberResponse;
import com.example.commute.dto.SignupRequestDto;
import com.example.commute.entity.Member;
import com.example.commute.enums.RoleType;
import com.example.commute.repository.MemberRepository; // 'memberRepository'를 'MemberRepository'로 수정
import com.example.commute.service.JwtService;
import com.example.commute.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MemberRepository memberRepository; // 수정된 부분

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final MemberService memberService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> authenticateUser(@RequestBody LoginRequestDto loginRequestDto) {
        // 🔹 1. 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 🔹 2. 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberService.findByUsername(userDetails.getUsername());

        // 🔹 3. JWT 토큰 생성
        String token = jwtService.generateToken(userDetails);

        // 🔹 4. DTO 형태로 응답
        return ResponseEntity.ok(new MemberResponse(member.getUsername(), token));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signupRequestDto) {
        if (memberRepository.existsByUsername(signupRequestDto.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        Member member = new Member();
        member.setUsername(signupRequestDto.getUsername());
        member.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));

        member.setRole(RoleType.USER);
        memberRepository.save(member);
        return ResponseEntity.ok("User registered successfully!");
    }
}
