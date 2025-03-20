package com.example.commute.service;

import com.example.commute.dto.ExpenseDto;
import com.example.commute.entity.Expense;
import com.example.commute.entity.User;
import com.example.commute.repository.ExpenseRepository;
import com.example.commute.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    // 모든 게시글 목록 조회 (페이지네이션 추가)
    public Page<ExpenseDto> getAllExpenses(Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findAllByOrderByCreatedAtDesc(pageable);
        return expenses.map(this::convertToDto);
    }

    // 특정 사용자가 작성한 게시글 목록 조회 (페이지네이션 추가)
    public Page<ExpenseDto> getUserExpenses(User user, Pageable pageable) {
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
    public ExpenseDto createExpense(ExpenseDto expenseDto, User user) {
        if (user == null || user.getId() == null || !userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("유효한 사용자 정보가 없습니다.");
        }

        Expense expense = new Expense();
        expense.setTitle(expenseDto.getTitle());
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getAmount());
        expense.setUser(user);  // User 연결

        expenseRepository.save(expense);

        return convertToDto(expense);
    }

    // 게시글 수정
    public ExpenseDto updateExpense(Long id, ExpenseDto expenseDto, User user) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!expense.getUser().equals(user)) {
            throw new IllegalArgumentException("작성자만 게시글을 수정할 수 있습니다.");
        }

        expense.setTitle(expenseDto.getTitle());
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getAmount());
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));
        expense.setPhotoUrl(expenseDto.getPhotoUrl());

        Expense updatedExpense = expenseRepository.save(expense);
        return convertToDto(updatedExpense);
    }

    // 게시글 삭제
    public void deleteExpense(Long id, User user) {
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
                .mapToDouble(ExpenseDto::getAmount)
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
                .amount(expense.getAmount())
                .category(String.valueOf(expense.getCategory()))
                .photoUrl(expense.getPhotoUrl())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    // ExpenseDto를 Expense로 변환하는 메서드
    private Expense convertToEntity(ExpenseDto expenseDto) {
        return Expense.builder()
                .id(expenseDto.getId())
                .title(expenseDto.getTitle())
                .content(expenseDto.getContent())
                .amount(expenseDto.getAmount())
                .category(Expense.Category.valueOf(expenseDto.getCategory()))
                .photoUrl(expenseDto.getPhotoUrl())
                .createdAt(expenseDto.getCreatedAt())
                .build();
    }

    public ExpenseDto getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return convertToDto(expense);
    }
}
