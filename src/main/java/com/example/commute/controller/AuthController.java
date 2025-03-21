package com.example.commute.controller;

import com.example.commute.dto.LoginRequestDto;
import com.example.commute.dto.SignupRequestDto;
import com.example.commute.entity.Member;
import com.example.commute.enums.RoleType;
import com.example.commute.repository.MemberRepository; // 'memberRepository'를 'MemberRepository'로 수정
import com.example.commute.service.JwtService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MemberRepository memberRepository; // 수정된 부분

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signupRequestDto) {
        if (memberRepository.existsByUsername(signupRequestDto.getUsername())) { // 수정된 부분
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        Member member = new Member(); // 수정된 부분
        member.setUsername(signupRequestDto.getUsername()); // 수정된 부분
        member.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        member.setRole(RoleType.USER); // 수정된 부분
        memberRepository.save(member); // 수정된 부분
        return ResponseEntity.ok("User registered successfully!");
    }
}
