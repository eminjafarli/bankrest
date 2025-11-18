package com.project.bankrest.Controller;

import com.project.bankrest.DTO.CardResponse;
import com.project.bankrest.DTO.TransferRequest;
import com.project.bankrest.DTO.CreateCardRequest;
import com.project.bankrest.DTO.UpdateStatusRequest;
import com.project.bankrest.Entity.Card;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    private CardResponse toDTO(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setNumber(card.getNumber());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setCvv(card.getCvv());
        dto.setBalance(card.getBalance().toString());

        if (card.getUser() != null) {
            dto.setUserId(card.getUser().getId());
            dto.setUserName(card.getUser().getName());
        }
        return dto;
    }

    @Tag(name = "Карты (Администратор)", description = "Операции управления картами, доступные только ADMIN")

    @Operation(
            summary = "ADMIN: Создать новую карту",
            description = "Создает новую карту и привязывает её к пользователю.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой карты",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCardRequest.class),
                            examples = @ExampleObject("""
                                    {
                                      "userId": 3,
                                      "number": "5321240011223344",
                                      "expirationDate": "2028-05",
                                      "cvv": "771",
                                      "status": "ACTIVE",
                                      "balance": "5000"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Карта успешно создана")
    @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<Card> createCard(@RequestBody CreateCardRequest request) {
        Card newCard = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
    }


    @Operation(
            summary = "ADMIN: Обновить статус карты",
            description = "Изменяет статус карты, например ACTIVE → BLOCKED.",
            parameters = {
                    @Parameter(name = "cardId", description = "ID карты", example = "10")
            }
    )
    @ApiResponse(responseCode = "200", description = "Статус обновлён")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{cardId}/status")
    public ResponseEntity<Card> updateCardStatus(
            @PathVariable Long cardId,
            @RequestBody UpdateStatusRequest request
    ) {
        Card updated = cardService.updateCardStatus(cardId, request.getStatus());
        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "ADMIN: Обновить данные карты",
            description = "Изменяет поля существующей карты.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCardRequest.class))
            )
    )
    @ApiResponse(responseCode = "200", description = "Данные обновлены")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{cardId}")
    public ResponseEntity<Card> updateCard(
            @PathVariable Long cardId,
            @RequestBody CreateCardRequest request
    ) {
        Card updatedCard = cardService.updateCard(cardId, request);
        return ResponseEntity.ok(updatedCard);
    }


    @Operation(
            summary = "ADMIN: Удалить карту",
            description = "Полностью удаляет карту из системы."
    )
    @ApiResponse(responseCode = "200", description = "Карта удалена")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "ADMIN: Получить список всех карт",
            description = "Возвращает список всех карт в системе.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешно",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = CardResponse.class))
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<CardResponse>> getAllCards() {
        List<CardResponse> list = cardService.getAllCards()
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Tag(name = "Карты (Пользователь)", description = "Операции с картами текущего пользователя")

    @Operation(
            summary = "ADMIN/USER: Получить карты по ID пользователя",
            description = "Возвращает карты указанного пользователя.\n" +
                    "● ADMIN: может запрашивать любого\n" +
                    "● USER: только свой собственный ID",
            parameters = {
                    @Parameter(name = "userId", example = "3", description = "ID пользователя")
            }
    )
    @ApiResponse(responseCode = "200", description = "Список карт")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponse>> getCardsByUserId(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        List<CardResponse> list = cardService.getAllCardsByUserId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(list);
    }


    @Operation(
            summary = "USER: Получить собственные карты",
            description = "Возвращает собственные карты пользователя. Поддерживается пагинация и поиск.",
            parameters = {
                    @Parameter(name = "search", description = "Поиск (по номеру, статусу и т.п.)")
            }
    )
    @ApiResponse(responseCode = "200", description = "Список карт")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @GetMapping("/my")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(required = false) String search,
            Pageable pageable,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        Page<CardResponse> result = cardService
                .getUserCards(currentUser.getId(), search, pageable)
                .map(this::toDTO);

        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "USER: Проверить баланс карты",
            description = "Возвращает текущий баланс карты, принадлежащей пользователю."
    )
    @ApiResponse(responseCode = "200", description = "Баланс возвращён")
    @ApiResponse(responseCode = "404", description = "Карта не найдена или не принадлежит пользователю")
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable Long cardId,
            Authentication auth
    ) {
        User currentUser = (User) auth.getPrincipal();
        BigDecimal balance = cardService.getCardBalance(cardId, currentUser.getId());
        return ResponseEntity.ok(balance);
    }


    @Operation(
            summary = "USER: Создать новую карту",
            description = "Создаёт карту и автоматически привязывает её к текущему пользователю."
    )
    @ApiResponse(responseCode = "201", description = "Карта создана")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<Card> createMyCard(
            @RequestBody CreateCardRequest request,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        request.setUserId(currentUser.getId());
        Card newCard = cardService.createCard(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
    }


    @Operation(
            summary = "USER: Запросить блокировку карты",
            description = "Меняет статус карты на BLOCK_REQUESTED."
    )
    @ApiResponse(responseCode = "200", description = "Запрос отправлен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @PatchMapping("/{cardId}/request-block")
    public ResponseEntity<Card> requestCardBlock(
            @PathVariable Long cardId,
            Authentication auth
    ) {
        User currentUser = (User) auth.getPrincipal();
        Card updated = cardService.requestCardBlock(cardId, currentUser.getId());

        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "USER: Отменить запрос на блокировку карты",
            description = "Отменяет ранее отправленный запрос BLOCK_REQUESTED."
    )
    @ApiResponse(responseCode = "200", description = "Запрос отменён")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{cardId}/cancel-request")
    public ResponseEntity<Card> cancelBlockRequest(
            @PathVariable Long cardId,
            Authentication auth
    ) {
        User currentUser = (User) auth.getPrincipal();
        Card updated = cardService.cancelCardBlockRequest(cardId, currentUser.getId());

        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "USER: Перевод средств",
            description = "Выполняет перевод средств между картами пользователя.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Параметры перевода",
                    content = @Content(
                            schema = @Schema(implementation = TransferRequest.class),
                            examples = @ExampleObject("""
                                    {
                                      "fromCardId": 5,
                                      "toCardId": 8,
                                      "amount": "150.00"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Перевод выполнен")
    @ApiResponse(responseCode = "400", description = "Ошибка перевода")
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody TransferRequest request) {
        try {
            cardService.transferMoney(request);
            return ResponseEntity.ok("Transfer successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
