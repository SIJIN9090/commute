package com.example.commute.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmountDto {
    private String amount;

    @JsonCreator
    public AmountDto(@JsonProperty("amount") String amount) {
        this.amount = amount;
    }
}