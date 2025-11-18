package com.project.bankrest;

import com.project.bankrest.DTO.CreateCardRequest;
import com.project.bankrest.DTO.TransferRequest;
import com.project.bankrest.Entity.Card;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.CardRepository;
import com.project.bankrest.Repository.UserRepository;

import com.project.bankrest.Services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;
    private User testUser;
    private Card sourceCard;
    private Card destCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setName("Test User");

        sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setBalance(new BigDecimal("1000.00"));
        sourceCard.setStatus("ACTIVE");
        sourceCard.setUser(testUser);

        destCard = new Card();
        destCard.setId(2L);
        destCard.setBalance(new BigDecimal("500.00"));
        destCard.setStatus("ACTIVE");
        destCard.setUser(testUser);
    }

    @Test
    void createCard_ValidRequest_ShouldReturnSavedCard() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(10L);
        request.setNumber("1234567890123456");
        request.setCvv("123");
        request.setBalance("50.00");
        request.setExpirationDate("12/26");

        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(5L);
            return card;
        });

        Card result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(0, new BigDecimal("50.00").compareTo(result.getBalance()));
        assertEquals(testUser, result.getUser());

        verify(userRepository, times(1)).findById(10L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_UserNotFound_ShouldThrowRuntimeException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cardService.createCard(request);
        }, "User not found");

        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_NullUserId_ShouldThrowIllegalArgumentException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(request);
        }, "User ID must not be null");

        verify(userRepository, never()).findById(anyLong());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCardStatus_ValidIdAndStatus_ShouldUpdateStatus() {
        String newStatus = "BLOCKED";
        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sourceCard);
        Card updatedCard = cardService.updateCardStatus(1L, newStatus);
        assertEquals(newStatus, updatedCard.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(sourceCard);
    }

    @Test
    void deleteCard_ValidId_ShouldCallDeleteById() {
        cardService.deleteCard(1L);
        verify(cardRepository, times(1)).deleteById(1L);
    }

    @Test
    void getUserCards_NoSearch_ShouldReturnPaginatedCards() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cardList = Arrays.asList(sourceCard, destCard);
        Page<Card> expectedPage = new PageImpl<>(cardList, pageable, 2);
        when(cardRepository.findByUser_IdWithUserEagerly(10L, pageable)).thenReturn(expectedPage);
        Page<Card> result = cardService.getUserCards(10L, null, pageable);
        assertEquals(2, result.getTotalElements());
        assertEquals(sourceCard, result.getContent().get(0));
        verify(cardRepository, times(1)).findByUser_IdWithUserEagerly(10L, pageable);
        verify(cardRepository, never()).findByUser_IdAndNumberContainingWithUserEagerly(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    void getUserCards_WithSearch_ShouldReturnSearchedPaginatedCards() {
        String search = "123";
        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cardList = Arrays.asList(sourceCard);
        Page<Card> expectedPage = new PageImpl<>(cardList, pageable, 1);
        when(cardRepository.findByUser_IdAndNumberContainingWithUserEagerly(10L, search, pageable)).thenReturn(expectedPage);
        Page<Card> result = cardService.getUserCards(10L, search, pageable);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository, times(1)).findByUser_IdAndNumberContainingWithUserEagerly(10L, search, pageable);
        verify(cardRepository, never()).findByUser_IdWithUserEagerly(anyLong(), any(Pageable.class));
    }

    @Test
    void getCardBalance_CardOwnedByUser_ShouldReturnBalance() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        BigDecimal balance = cardService.getCardBalance(1L, 10L);
        assertEquals(0, new BigDecimal("1000.00").compareTo(balance));
        verify(cardRepository, times(1)).findById(1L);
    }

    @Test
    void getCardBalance_CardNotOwnedByUser_ShouldThrowSecurityException() {
        Long hackerId = 99L;

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));

        assertThrows(SecurityException.class, () -> {
            cardService.getCardBalance(1L, hackerId);
        }, "Access denied: Card does not belong to the user.");
    }


    @Test
    void transferMoney_SuccessfulTransfer_ShouldUpdateBalances() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destCard));

        cardService.transferMoney(request);

        assertEquals(0, new BigDecimal("900.00").compareTo(sourceCard.getBalance()));
        assertEquals(0, new BigDecimal("600.00").compareTo(destCard.getBalance()));

        verify(cardRepository, times(1)).save(sourceCard);
        verify(cardRepository, times(1)).save(destCard);
    }

    @Test
    void transferMoney_InsufficientFunds_ShouldThrowException() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10000.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destCard));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.transferMoney(request);
        }, "Insufficient funds.");

        assertEquals(0, new BigDecimal("1000.00").compareTo(sourceCard.getBalance()));
        assertEquals(0, new BigDecimal("500.00").compareTo(destCard.getBalance()));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_InactiveSourceCard_ShouldThrowException() {
        sourceCard.setStatus("BLOCKED");
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destCard));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.transferMoney(request);
        }, "One or both cards are not active.");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_NegativeAmount_ShouldThrowException() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("-10.00"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.transferMoney(request);
        }, "Transfer amount must be positive.");

        verify(cardRepository, never()).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_SameCardIds_ShouldThrowException() {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.transferMoney(request);
        }, "Cannot transfer to the same card.");

        verify(cardRepository, never()).findById(anyLong());
    }
}