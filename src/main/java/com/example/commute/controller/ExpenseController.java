package com.example.commute.controller;

import com.example.commute.dto.ExpenseDto;
import com.example.commute.entity.Expense;
import com.example.commute.entity.Member;
import com.example.commute.entity.Photo;
import com.example.commute.repository.MemberRepository;
import com.example.commute.repository.PhotoRepository;
import com.example.commute.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    // 게시글 목록 조회 (관리자는 전체 목록, 일반 사용자는 자신만 조회)
    @GetMapping
    public ResponseEntity<Page<ExpenseDto>> getAllExpenses(
            @AuthenticationPrincipal Member member,
            @PageableDefault(size = 10) Pageable pageable) {

        // 관리자는 전체 목록 조회, 일반 사용자는 자신의 목록만 조회
        if (member.isAdmin()) {
            return ResponseEntity.ok(expenseService.getAllExpenses(pageable));
        } else {
            return ResponseEntity.ok(expenseService.getMemberExpenses(member, pageable));
        }
    }

    // 게시글 작성
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<ExpenseDto> createExpense(
            @RequestPart("expenseDto") ExpenseDto expenseDto,
            @RequestPart("files") List<MultipartFile> files) {

        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // 인증되지 않으면 FORBIDDEN 상태 반환
            }

            String username = authentication.getName();
            Optional<Member> optionalMember = memberRepository.findByUsername(username);
            Member member = optionalMember.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            // 파일 업로드 처리 (파일이 있을 경우에만 처리)
            if (files != null && !files.isEmpty()) {
                List<String> photoUrls = new ArrayList<>();
                for (MultipartFile file : files) {
                    // 파일 업로드 처리
                    String photoUrl = uploadFile(file, member);
                    photoUrls.add(photoUrl);
                }
                expenseDto.setPhotoUrls(photoUrls);  // 파일 URL 리스트 설정
            } else {
                expenseDto.setPhotoUrls(new ArrayList<>());  // 파일이 없을 경우 빈 리스트로 설정
            }

            // ExpenseService를 통해 게시글 생성
            ExpenseDto createdExpense = expenseService.createExpense(expenseDto);

            return ResponseEntity.ok(createdExpense);  // 생성된 게시글 반환
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 잘못된 JSON 형식일 경우 처리
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 예기치 못한 예외 처리
        }
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
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestBody ExpenseDto expenseDto,
            @AuthenticationPrincipal Member member) {

        try {
            ExpenseDto existingExpense = expenseService.getExpenseById(id);

            // 새 파일이 있을 경우 파일 업로드 처리
            if (files != null && !files.isEmpty()) {
                List<String> newPhotoUrls = new ArrayList<>();
                for (MultipartFile file : files) {
                    String photoUrl = uploadFile(file, member);
                    newPhotoUrls.add(photoUrl);
                }
                expenseDto.setPhotoUrls(newPhotoUrls);
            } else {
                expenseDto.setPhotoUrls(existingExpense.getPhotoUrls());  // 기존 사진 URL 유지
            }

            // ExpenseService를 통해 게시글 수정
            ExpenseDto updatedExpense = expenseService.updateExpense(id, expenseDto, member);
            return ResponseEntity.ok(updatedExpense);  // 수정된 게시글 반환
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 잘못된 JSON 형식일 경우 처리
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 예기치 못한 예외 처리
        }
    }

    // 게시글 삭제 (관리자만 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, @AuthenticationPrincipal Member member) {
        if (!member.isAdmin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 관리자만 삭제 가능, 401 Unauthorized 반환
        }

        try {
            expenseService.deleteExpense(id, member);  // ExpenseService로 삭제 요청
            return ResponseEntity.noContent().build();  // 성공적으로 삭제되었을 때
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 예기치 못한 예외 처리
        }
    }

    // 합산된 금액을 서버에서 계산
    @PostMapping("/total")
    public ResponseEntity<Double> getTotalAmount(@RequestBody List<ExpenseDto> expenses) {
        try {
            Double totalAmount = expenseService.calculateTotalAmountForDtos(expenses);  // ExpenseDto에 대한 합산 계산
            return ResponseEntity.ok(totalAmount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 예기치 못한 예외 처리
        }
    }

    // 카테고리로 검색하는 API
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ExpenseDto>> getExpensesByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable) {

        try {
            Page<ExpenseDto> expenses = expenseService.getExpensesByCategory(Expense.Category.valueOf(category), pageable);
            return ResponseEntity.ok(expenses);  // 카테고리별로 조회된 목록 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 잘못된 카테고리 입력 처리
        }
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 업로드 메서드
    public String uploadFile(MultipartFile file, Member member) throws IOException {
        // 파일 이름, 경로, 타입, 크기 등을 설정
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + "/" + UUID.randomUUID() + "_" + fileName;
        String fileType = file.getContentType();
        Long fileSize = file.getSize();
        LocalDateTime uploadedAt = LocalDateTime.now();

        // 파일을 실제로 저장하는 코드 (파일 시스템에 저장)
        File destinationFile = new File(filePath);
        file.transferTo(destinationFile);

        // Member의 Expense 리스트에서 적합한 Expense를 가져와서 연결할 수 있는지 확인
        // 이 예시에서는 첫 번째 Expense를 연결한다고 가정 (여러 개의 Expense가 있을 수 있으므로 적절한 로직으로 수정 필요)
        Expense expense = member.getExpenses().get(0); // 실제로는 Expense를 선택하는 로직이 필요

        // Photo 객체 생성 후 저장
        Photo photo = new Photo(fileName, filePath, expense);  // Expense 객체를 연결
        photo.setFileType(fileType);
        photo.setFileSize(fileSize);
        photo.setUploadedAt(uploadedAt);

        photoRepository.save(photo);  // PhotoRepository를 통해 DB에 저장

        return filePath;  // 업로드된 파일의 경로 반환
    }

}
