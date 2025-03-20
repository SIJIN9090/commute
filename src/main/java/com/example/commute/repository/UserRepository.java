package com.example.commute.repository;

import com.example.commute.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username); // Optional 반환하도록 변경
    boolean existsByUsername(String username);
}
