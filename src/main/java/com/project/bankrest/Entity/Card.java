package com.project.bankrest.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.bankrest.Security.EncryptBigDecimalConverter;
import com.project.bankrest.Security.EncryptStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptStringConverter.class)
    @Column(name = "card_number", unique = true)
    private String number;

    @Convert(converter = EncryptStringConverter.class)
    @Column(name = "date")
    private String expirationDate;

    private String status;

    @Convert(converter = EncryptStringConverter.class)
    private String cvv;

    @Convert(converter = EncryptBigDecimalConverter.class)
    private BigDecimal balance = BigDecimal.ZERO;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
