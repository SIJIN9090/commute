package com.example.commute.service;

import com.example.commute.dto.ExpenseDto;
import com.example.commute.entity.Expense;
import com.example.commute.entity.Member;
import com.example.commute.entity.Photo;
import com.example.commute.repository.ExpenseRepository;
import com.example.commute.repository.MemberRepository;
import com.example.commute.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    // 게시글 작성
    @Transactional
    public ExpenseDto createExpense(ExpenseDto expenseDto) {
        logger.info("Creating new expense with title: {}", expenseDto.getTitle());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Authentication failed: user is not authenticated.");
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        String username = authentication.getName(); // 로그인된 사용자의 이름
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        Member member = optionalMember.orElseThrow(() -> {
            logger.error("User not found: {}", username);
            return new IllegalArgumentException("존재하지 않는 사용자입니다.");
        });

        // Expense 객체 생성
        Expense expense = new Expense();
        expense.setTitle(expenseDto.getTitle());
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getTotalAmount());
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));
        expense.setMember(member);

        // 사진 URL을 Photo 엔티티로 변환하여 저장
        if (expenseDto.getPhotoUrls() != null && !expenseDto.getPhotoUrls().isEmpty()) {
            logger.info("Adding photos to expense: {}", expenseDto.getPhotoUrls());
            List<Photo> photos = expenseDto.getPhotoUrls().stream()
                    .map(url -> Photo.builder()
                            .fileName(url)
                            .filePath(url)
                            .fileType("image/jpeg")  // 예시로 설정
                            .fileSize(102400L)  // 예시로 설정
                            .uploadedAt(LocalDateTime.now())
                            .description("Expense photo")
                            .expense(expense)
                            .build())
                    .collect(Collectors.toList());
            // Photo 객체를 저장한 후, Expense에 설정
            photoRepository.saveAll(photos);
            expense.setPhotos(photos);
        }

        // Expense 저장
        Expense savedExpense = expenseRepository.save(expense);
        logger.info("Expense created with ID: {}", savedExpense.getId());

        return convertToDto(savedExpense);
    }

    // 게시글 수정
    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseDto expenseDto, Member member) {
        logger.info("Updating expense with ID: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", id);
                    return new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
                });

        if (!expense.getMember().equals(member)) {
            logger.error("User is not the author of the expense: {}", member.getUsername());
            throw new IllegalArgumentException("작성자만 게시글을 수정할 수 있습니다.");
        }

        expense.setTitle(expenseDto.getTitle());
        expense.setContent(expenseDto.getContent());
        expense.setAmount(expenseDto.getTotalAmount());
        expense.setCategory(Expense.Category.valueOf(expenseDto.getCategory()));

        // 사진 URL을 처리
        if (expenseDto.getPhotoUrls() != null && !expenseDto.getPhotoUrls().isEmpty()) {
            logger.info("Updating photos for expense with ID: {}", id);
            List<Photo> photos = expenseDto.getPhotoUrls().stream()
                    .map(url -> Photo.builder()
                            .fileName(url)
                            .filePath(url)
                            .expense(expense)
                            .build())
                    .collect(Collectors.toList());
            // 기존 사진을 삭제하고 새로 저장
            photoRepository.deleteAll(expense.getPhotos());
            photoRepository.saveAll(photos);
            expense.setPhotos(photos);
        }

        Expense updatedExpense = expenseRepository.save(expense);
        logger.info("Expense updated with ID: {}", updatedExpense.getId());

        return convertToDto(updatedExpense);
    }

    // 게시글 삭제
    @Transactional
    public void deleteExpense(Long id, Member member) {
        logger.info("Deleting expense with ID: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", id);
                    return new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
                });

        if (!expense.getMember().equals(member)) {
            logger.error("User is not the author of the expense: {}", member.getUsername());
            throw new IllegalArgumentException("작성자만 게시글을 삭제할 수 있습니다.");
        }

        // 연관된 사진 삭제
        photoRepository.deleteAll(expense.getPhotos());

        expenseRepository.delete(expense);
        logger.info("Expense deleted with ID: {}", id);
    }

    // 게시글 조회 (단일)
    public ExpenseDto getExpenseById(Long id) {
        logger.info("Fetching expense with ID: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", id);
                    return new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
                });
        return convertToDto(expense);
    }

    // 모든 게시글 조회 (관리자용)
    public Page<ExpenseDto> getAllExpenses(Pageable pageable) {
        logger.info("Fetching all expenses with pagination: {}", pageable);

        return expenseRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    // 특정 사용자의 게시글 조회
    public Page<ExpenseDto> getMemberExpenses(Member member, Pageable pageable) {
        logger.info("Fetching expenses for member: {} with pagination: {}", member.getUsername(), pageable);

        return expenseRepository.findByMember(member, pageable)
                .map(this::convertToDto);
    }

    // 금액 항목을 서버에 저장하는 함수
    public void saveExpenses(List<ExpenseDto> expenses) {
        logger.info("Saving multiple expense entries.");

        if (expenses == null || expenses.isEmpty()) {
            logger.error("No expenses to save.");
            throw new IllegalArgumentException("저장할 비용 항목이 없습니다.");
        }

        List<Expense> expenseEntities = convertDtoListToEntityList(expenses);
        expenseRepository.saveAll(expenseEntities);
        logger.info("Multiple expenses saved.");
    }

    // 합산된 금액을 계산하는 함수 (ExpenseDto 리스트에서 계산)
    public Double calculateTotalAmountForDtos(List<ExpenseDto> expenses) {
        logger.info("Calculating total amount for expenses.");

        return expenses.stream()
                .mapToDouble(ExpenseDto::getTotalAmount)
                .sum();
    }

    // 카테고리별 비용 조회
    public Page<ExpenseDto> getExpensesByCategory(Expense.Category category, Pageable pageable) {
        logger.info("Fetching expenses by category: {} with pagination: {}", category, pageable);

        return expenseRepository.findByCategory(category, pageable)
                .map(this::convertToDto);
    }

    // List<ExpenseDto>를 List<Expense>로 변환하는 메서드
    public List<Expense> convertDtoListToEntityList(List<ExpenseDto> expenseDtos) {
        logger.info("Converting ExpenseDto list to Expense entity list.");

        return expenseDtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    // Expense 엔티티를 DTO로 변환하는 메서드
    private ExpenseDto convertToDto(Expense expense) {
        logger.debug("Converting Expense entity to DTO with ID: {}", expense.getId());

        List<String> photoUrls = Optional.ofNullable(expense.getPhotos())
                .orElse(Collections.emptyList())
                .stream()
                .map(Photo::getFilePath)
                .collect(Collectors.toList());

        return ExpenseDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .content(expense.getContent())
                .totalAmount(expense.getAmount())
                .category(String.valueOf(expense.getCategory()))
                .photoUrls(photoUrls)
                .createdAt(expense.getCreatedAt())
                .build();
    }

    // ExpenseDto를 Expense로 변환하는 메서드
    private Expense convertToEntity(ExpenseDto expenseDto) {
        logger.debug("Converting ExpenseDto to entity with ID: {}", expenseDto.getId());

        Expense expense = Expense.builder()
                .id(expenseDto.getId())
                .title(expenseDto.getTitle())
                .content(expenseDto.getContent())
                .amount(expenseDto.getTotalAmount())
                .category(Expense.Category.valueOf(expenseDto.getCategory()))
                .createdAt(expenseDto.getCreatedAt())
                .build();

        if (expenseDto.getPhotoUrls() != null && !expenseDto.getPhotoUrls().isEmpty()) {
            List<Photo> photos = expenseDto.getPhotoUrls().stream()
                    .map(url -> Photo.builder()
                            .fileName(url)
                            .filePath(url)
                            .expense(expense)
                            .build())
                    .collect(Collectors.toList());
            expense.setPhotos(photos);
        }

        return expense;
    }
}
