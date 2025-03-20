package com.example.commute.controller;

import com.example.commute.dto.ExpenseDto;
import com.example.commute.entity.Expense;
import com.example.commute.entity.Member;
import com.example.commute.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // 게시글 목록 조회 (관리자는 전체 목록, 일반 사용자는 자신만 조회)
    @GetMapping
    public ResponseEntity<Page<ExpenseDto>> getAllExpenses(
            @AuthenticationPrincipal Member user,
            @PageableDefault(size = 10) Pageable pageable) {  // Pageable을 자동으로 주입받음

        if (user.isAdmin()) {
            return ResponseEntity.ok(expenseService.getAllExpenses(pageable));  // 관리자면 전체 목록 조회
        } else {
            return ResponseEntity.ok(expenseService.getUserExpenses(user, pageable));  // 일반 사용자는 자신만 조회
        }
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("expenseDto") String expenseDtoJson,
            @AuthenticationPrincipal Member user) throws IOException {

        ExpenseDto expenseDto = convertJsonToExpenseDto(expenseDtoJson);
        if (expenseDto.getTotalAmount() == null) {
            expenseDto.setTotalAmount(0.0);
        }

        if (files != null && !files.isEmpty()) {
            List<String> photoUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String photoUrl = uploadFile(file);
                if (!photoUrl.isEmpty()) { // 빈 문자열이 아닐 경우에만 추가
                    photoUrls.add(photoUrl);
                }
            }
            expenseDto.setPhotoUrls(photoUrls);
        }

        ExpenseDto createdExpense = expenseService.createExpense(expenseDto, user);
        return ResponseEntity.ok(createdExpense);
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("expenseDto") String expenseDtoJson,
            @AuthenticationPrincipal Member user) throws IOException {

        ExpenseDto expenseDto = convertJsonToExpenseDto(expenseDtoJson);
        ExpenseDto existingExpense = expenseService.getExpenseById(id);

        if (file != null && !file.isEmpty()) {
            String photoUrl = uploadFile(file);
            expenseDto.setPhotoUrls(List.of(photoUrl));
        } else {
            expenseDto.setPhotoUrls(existingExpense.getPhotoUrls()); // 기존 사진 URL 유지
        }

        return ResponseEntity.ok(expenseService.updateExpense(id, expenseDto, user));
    }


    // 게시글 삭제 (관리자만 삭제 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, @AuthenticationPrincipal Member user) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(401).build();  // 401 Unauthorized
        }
        expenseService.deleteExpense(id, user);
        return ResponseEntity.noContent().build();
    }

    // 합산된 금액을 서버에서 계산 (ExpenseDto 사용)
    @PostMapping("/total")
    public ResponseEntity<Double> getTotalAmount(@RequestBody List<ExpenseDto> expenses) {
        Double totalAmount = expenseService.calculateTotalAmountForDtos(expenses);  // ExpenseDto에 대한 계산
        return ResponseEntity.ok(totalAmount);
    }

    // 카테고리로 검색하는 API 추가
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ExpenseDto>> getExpensesByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable) {  // Pageable을 자동으로 주입받음
        Page<ExpenseDto> expenses = expenseService.getExpensesByCategory(Expense.Category.valueOf(category), pageable);
        return ResponseEntity.ok(expenses);
    }
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 업로드 API
    private String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ""; // 파일이 비어있을 경우 빈 문자열 반환
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path path = Paths.get(uploadDir + fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        return "/uploads/" + fileName;
    }

    // ExpenseDto를 JSON 문자열에서 객체로 변환하는 메서드 (예시로 ObjectMapper 사용)
    private ExpenseDto convertJsonToExpenseDto(String expenseDtoJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(expenseDtoJson, ExpenseDto.class);
    }
}
