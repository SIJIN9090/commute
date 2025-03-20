package com.example.commute.repository;

import com.example.commute.entity.Expense;
import com.example.commute.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 전체 비용 목록 최신순으로 조회 (페이지네이션 추가)
    Page<Expense> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 사용자의 비용 목록 조회 (페이지네이션 추가)
    Page<Expense> findByUser(Member user, Pageable pageable);

    // 카테고리로 필터링하는 메서드 (페이지네이션 추가)
    public Page<Expense> findByCategory(Expense.Category category, Pageable pageable);

}
