package com.project.bankrest.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CardDTO {
    private Long id;
    private String number;
    private String expirationDate;
    private String status;
    private String cvv;
    private String balance;
    private Long userId;
    private String userName;
}
