package com.project.bankrest;

import com.project.bankrest.Controller.CardController;
import com.project.bankrest.DTO.CardResponse;
import com.project.bankrest.DTO.CreateCardRequest;
import com.project.bankrest.DTO.TransferRequest;
import com.project.bankrest.DTO.UpdateStatusRequest;
import com.project.bankrest.Entity.Card;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Services.CardService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardController cardController;

    private User mockAuthentication(Long userId) {
        User user = new User();
        user.setId(userId);
        when(authentication.getPrincipal()).thenReturn(user);
        return user;
    }

    @Test
    void testCreateCardAdmin() {
        CreateCardRequest req = new CreateCardRequest();
        req.setUserId(1L);
        req.setNumber("1234");
        req.setCvv("111");

        Card newCard = new Card();
        newCard.setId(10L);
        newCard.setNumber("1234");

        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(newCard);

        ResponseEntity<Card> response = cardController.createCard(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        verify(cardService, times(1)).createCard(req);
    }

    @Test
    void testUpdateCardStatus() {
        Long cardId = 10L;
        String newStatus = "BLOCKED";
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(newStatus);

        Card updated = new Card();
        updated.setId(cardId);
        updated.setStatus(newStatus);

        when(cardService.updateCardStatus(eq(cardId), eq(newStatus))).thenReturn(updated);

        ResponseEntity<Card> response = cardController.updateCardStatus(cardId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newStatus, response.getBody().getStatus());
        verify(cardService, times(1)).updateCardStatus(cardId, newStatus);
    }

    @Test
    void testUpdateCard() {
        Long cardId = 10L;
        CreateCardRequest request = new CreateCardRequest();
        request.setNumber("4321");

        Card updated = new Card();
        updated.setId(cardId);
        updated.setNumber("4321");

        when(cardService.updateCard(eq(cardId), any(CreateCardRequest.class))).thenReturn(updated);

        ResponseEntity<Card> response = cardController.updateCard(cardId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("4321", response.getBody().getNumber());
        verify(cardService, times(1)).updateCard(cardId, request);
    }

    @Test
    void testDeleteCard() {
        Long cardId = 10L;
        doNothing().when(cardService).deleteCard(cardId);

        ResponseEntity<Void> response = cardController.deleteCard(cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService, times(1)).deleteCard(cardId);
    }

    @Test
    void testGetAllCardsAdmin() {
        Card card = new Card();
        card.setId(1L);
        card.setNumber("1111");

        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);

        when(cardService.getAllCards()).thenReturn(List.of(card));

        ResponseEntity<List<CardResponse>> response = cardController.getAllCards();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(cardService, times(1)).getAllCards();
    }

    @Test
    void testGetCardsByUserId() {
        Long requestedUserId = 5L;
        User user = new User();
        user.setId(requestedUserId);

        Card card = new Card();
        card.setId(1L);
        card.setNumber("2222");
        card.setUser(user);
        card.setBalance(BigDecimal.TEN);

        when(cardService.getAllCardsByUserId(requestedUserId)).thenReturn(List.of(card));

        ResponseEntity<List<CardResponse>> response = cardController.getCardsByUserId(requestedUserId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(cardService, times(1)).getAllCardsByUserId(requestedUserId);
    }


    @Test
    void testGetMyCards() {
        Long userId = 50L;
        mockAuthentication(userId);

        Card card = new Card();
        card.setId(3L);
        card.setNumber("9999");
        card.setBalance(BigDecimal.ZERO);

        Page<Card> page = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);
        String search = "9999";

        when(cardService.getUserCards(eq(userId), eq(search), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CardResponse>> response = cardController.getMyCards(search, pageable, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(cardService, times(1)).getUserCards(eq(userId), eq(search), any(Pageable.class));
    }

    @Test
    void testGetBalance() {
        Long cardId = 10L;
        Long userId = 20L;
        BigDecimal expectedBalance = new BigDecimal("150.50");
        mockAuthentication(userId);

        when(cardService.getCardBalance(cardId, userId)).thenReturn(expectedBalance);

        ResponseEntity<BigDecimal> response = cardController.getBalance(cardId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBalance, response.getBody());
        verify(cardService, times(1)).getCardBalance(cardId, userId);
    }

    @Test
    void testCreateMyCard() {
        Long userId = 100L;
        mockAuthentication(userId);
        CreateCardRequest req = new CreateCardRequest();
        req.setNumber("1111");

        Card newCard = new Card();
        newCard.setId(5L);

        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(newCard);

        ResponseEntity<Card> response = cardController.createMyCard(req, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getId());
        assertEquals(userId, req.getUserId());
        verify(cardService, times(1)).createCard(req);
    }

    @Test
    void testRequestCardBlock() {
        Long cardId = 5L;
        Long userId = 100L;
        mockAuthentication(userId);

        Card updated = new Card();
        updated.setId(cardId);
        updated.setStatus("BLOCK_REQUESTED");

        when(cardService.requestCardBlock(cardId, userId)).thenReturn(updated);

        ResponseEntity<Card> response = cardController.requestCardBlock(cardId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BLOCK_REQUESTED", response.getBody().getStatus());
        verify(cardService, times(1)).requestCardBlock(cardId, userId);
    }

    @Test
    void testCancelBlockRequest() {
        Long cardId = 5L;
        Long userId = 100L;
        mockAuthentication(userId);

        Card updated = new Card();
        updated.setId(cardId);
        updated.setStatus("ACTIVE");

        when(cardService.cancelCardBlockRequest(cardId, userId)).thenReturn(updated);

        ResponseEntity<Card> response = cardController.cancelBlockRequest(cardId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACTIVE", response.getBody().getStatus());
        verify(cardService, times(1)).cancelCardBlockRequest(cardId, userId);
    }

    @Test
    void testTransferMoneySuccess() {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(new BigDecimal("20"));

        doNothing().when(cardService).transferMoney(any(TransferRequest.class));

        ResponseEntity<String> response = cardController.transferMoney(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transfer successful", response.getBody());
        verify(cardService, times(1)).transferMoney(req);
    }

    @Test
    void testTransferMoneyBadRequest() {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(BigDecimal.TEN);
        String errorMessage = "Not enough balance";

        doThrow(new IllegalArgumentException(errorMessage))
                .when(cardService).transferMoney(any(TransferRequest.class));

        ResponseEntity<String> response = cardController.transferMoney(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(cardService, times(1)).transferMoney(req);
    }
}
