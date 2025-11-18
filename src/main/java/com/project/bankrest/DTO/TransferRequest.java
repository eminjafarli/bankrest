package com.project.bankrest.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
