package com.example.commute.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseDto {

    private Long id;
    private String title;
    private String category;
    private String content;
    private List<String> photoUrls;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private String username;
    private String date;
    private List<AmountDto> amounts; // amounts 필드 추가

    @JsonCreator
    public ExpenseDto(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("category") String category,
            @JsonProperty("content") String content,
            @JsonProperty("photoUrls") List<String> photoUrls,
            @JsonProperty("totalAmount") Double totalAmount,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("username") String username,
            @JsonProperty("date") String date,
            @JsonProperty("amounts") List<AmountDto> amounts
    ) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.photoUrls = photoUrls;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.username = username; // 필드명 변경에 맞춰 수정
        this.date = date;
        this.amounts = amounts; // amounts 필드 초기화
    }

}
