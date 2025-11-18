package com.project.bankrest.DTO;

import lombok.Data;

@Data
public class CardResponse {
    private Long id;
    private String number;
    private String expirationDate;
    private String status;
    private String cvv;
    private String balance;
    private Long userId;
    private String userName;
}
