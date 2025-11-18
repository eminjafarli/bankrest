package com.project.bankrest.Services;

import com.project.bankrest.DTO.CreateCardRequest;
import com.project.bankrest.DTO.TransferRequest;
import com.project.bankrest.Entity.Card;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.CardRepository;
import com.project.bankrest.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private Card getCardAndVerifyOwner(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found."));
        if (!card.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: Card does not belong to the user.");
        }
        return card;
    }

    @Transactional
    public Card createCard(CreateCardRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = new Card();
        card.setNumber(request.getNumber());
        card.setCvv(request.getCvv());
        card.setBalance(new BigDecimal(request.getBalance()));
        card.setExpirationDate(request.getExpirationDate());
        card.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        card.setUser(user);

        return cardRepository.save(card);
    }

    public List<Card> getAllCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    @Transactional
    public Card updateCard(Long cardId, CreateCardRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setNumber(request.getNumber());
        card.setCvv(request.getCvv());
        card.setBalance(new BigDecimal(request.getBalance()));
        card.setExpirationDate(request.getExpirationDate());
        card.setStatus(request.getStatus());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            card.setUser(user);
        }

        return cardRepository.save(card);
    }

    @Transactional
    public Card updateCardStatus(Long cardId, String status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found."));
        card.setStatus(status);
        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAllWithUserOrderByIdAsc();
    }

    public Page<Card> getUserCards(Long userId, String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return cardRepository.findByUser_IdAndNumberContainingWithUserEagerly(userId, search, pageable);
        }
        return cardRepository.findByUser_IdWithUserEagerly(userId, pageable);
    }

    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = getCardAndVerifyOwner(cardId, userId);
        return card.getBalance();
    }

    @Transactional
    public Card requestCardBlock(Long cardId, Long userId) {
        Card card = getCardAndVerifyOwner(cardId, userId);
        card.setStatus("BLOCK_REQUESTED");
        return cardRepository.save(card);
    }
    @Transactional
    public Card cancelCardBlockRequest(Long cardId, Long userId) {
        Card card = getCardAndVerifyOwner(cardId, userId);

        if (!"BLOCK_REQUESTED".equals(card.getStatus())) {
            throw new IllegalArgumentException("Card is not in BLOCK_REQUESTED status.");
        }

        card.setStatus("ACTIVE");
        return cardRepository.save(card);
    }

    @Transactional
    public void transferMoney(TransferRequest request) {
        Long sourceCardId = request.getFromCardId();
        Long destCardId = request.getToCardId();
        BigDecimal amount = request.getAmount();

        if (sourceCardId.equals(destCardId))
            throw new IllegalArgumentException("Cannot transfer to the same card.");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Transfer amount must be positive.");

        Card sourceCard = cardRepository.findById(sourceCardId)
                .orElseThrow(() -> new IllegalArgumentException("Source card not found"));
        Card destCard = cardRepository.findById(destCardId)
                .orElseThrow(() -> new IllegalArgumentException("Destination card not found"));

        if (!"ACTIVE".equals(sourceCard.getStatus()) || !"ACTIVE".equals(destCard.getStatus()))
            throw new IllegalArgumentException("One or both cards are not active.");

        if (sourceCard.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient funds.");

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destCard.setBalance(destCard.getBalance().add(amount));

        cardRepository.save(sourceCard);
        cardRepository.save(destCard);
    }

}
