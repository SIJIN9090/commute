package com.example.commute.service;

import com.example.commute.entity.Member;
import com.example.commute.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 🔹 사용자 이름으로 조회하는 메서드 추가
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
