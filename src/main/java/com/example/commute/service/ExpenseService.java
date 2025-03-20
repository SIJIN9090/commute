package com.example.commute.service;

import com.example.commute.dto.ExpenseDto;
import com.example.commute.entity.Expense;
import com.example.commute.entity.Member;
import com.example.commute.repository.ExpenseRepository;
import com.example.commute.repository.memberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final memberRepository userRepository;

    // 모든 게시글 목록 조회 (페이지네이션 추가)
    public Page<ExpenseDto> getAllExpenses(Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findAllByOrderByCreatedAtDesc(pageable);
        return expenses.map(this::convertToDto);
    }

    // 특정 사용자가 작성한 게시글 목록 조회 (페이지네이션 추가)
    public Page<ExpenseDto> getUserExpenses(Member user, Pageable pageable) {
        if (user == null) {
            throw new IllegalArgumentException("유효한 사용자 정보가 없습니다.");
        }
        Page<Expense> expenses = expenseRepository.findByUser(user, pageable);
        return expenses.map(this::convertToDto);
    }

    // 카테고리로 비용 항목을 필터링하여 반환 (페이지네이션 추가)
    public Page<ExpenseDto> getExpensesByCategory(Expense.Category category, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByCategory(category, pageable);
        return expenses.map(this::convertToDto);
    }


    // 게시글 작성
    public ExpenseDto createExpense(ExpenseDto expenseDto, Member user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("사용자 정보가 누락되었습니다.");
        }

        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Expense expense = new Expense();
        expense.setTitle(expenseDto.getTitle());
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getTotalAmount());
        expense.setUser(user);

        // photoUrls가 null이 아니면 설정
        if (expenseDto.getPhotoUrls() != null && !expenseDto.getPhotoUrls().isEmpty()) {
            expense.setPhotoUrls(expenseDto.getPhotoUrls()); // 여러 URL을 그대로 저장
        }

        expenseRepository.save(expense);

        return convertToDto(expense);
    }

    // 게시글 수정
    public ExpenseDto updateExpense(Long id, ExpenseDto expenseDto, Member user) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!expense.getUser().equals(user)) {
            throw new IllegalArgumentException("작성자만 게시글을 수정할 수 있습니다.");
        }

        expense.setTitle(expenseDto.getTitle());
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getTotalAmount());  // 합산된 금액만 사용
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));

        // 단일 photoUrl 대신, 여러 파일을 처리할 수 있도록 수정
        if (expenseDto.getPhotoUrls() != null && !expenseDto.getPhotoUrls().isEmpty()) {
            expense.setPhotoUrls(Collections.singletonList(String.join(",", expenseDto.getPhotoUrls()))); // 여러 URL을 콤마로 구분하여 저장
        }

        Expense updatedExpense = expenseRepository.save(expense);
        return convertToDto(updatedExpense);
    }

    // 게시글 삭제
    public void deleteExpense(Long id, Member user) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!expense.getUser().equals(user)) {
            throw new IllegalArgumentException("작성자만 게시글을 삭제할 수 있습니다.");
        }

        expenseRepository.delete(expense);
    }

    // 금액 항목을 서버에 저장하는 함수
    public void saveExpenses(List<ExpenseDto> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            throw new IllegalArgumentException("저장할 비용 항목이 없습니다.");
        }

        List<Expense> expenseEntities = convertDtoListToEntityList(expenses);
        expenseRepository.saveAll(expenseEntities);
    }

    // 합산된 금액을 계산하는 함수 (ExpenseDto 리스트에서 계산)
    public Double calculateTotalAmountForDtos(List<ExpenseDto> expenses) {
        return expenses.stream()
                .mapToDouble(ExpenseDto::getTotalAmount)  // totalAmount로 합계 계산
                .sum();
    }

    // List<ExpenseDto>를 List<Expense>로 변환하는 메서드
    public List<Expense> convertDtoListToEntityList(List<ExpenseDto> expenseDtos) {
        return expenseDtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    // Expense 엔티티를 DTO로 변환하는 메서드
    private ExpenseDto convertToDto(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .content(expense.getContent())
                .totalAmount(expense.getAmount())
                .category(String.valueOf(expense.getCategory()))
                .photoUrls(expense.getPhotoUrls())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    // ExpenseDto를 Expense로 변환하는 메서드
    private Expense convertToEntity(ExpenseDto expenseDto) {
        return Expense.builder()
                .id(expenseDto.getId())
                .title(expenseDto.getTitle())
                .content(expenseDto.getContent())
                .amount(expenseDto.getTotalAmount())
                .category(Expense.Category.valueOf(expenseDto.getCategory()))
                .photoUrls(expenseDto.getPhotoUrls()) //
                .createdAt(expenseDto.getCreatedAt())
                .build();
    }

    public ExpenseDto getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return convertToDto(expense);
    }
}
