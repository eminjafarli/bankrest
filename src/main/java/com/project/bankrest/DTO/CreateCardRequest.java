package com.project.bankrest.DTO;

import lombok.Data;

@Data
public class CreateCardRequest {
    private String number;
    private String cvv;
    private String expirationDate;
    private String status;
    private String balance;
    private Long userId;
}

