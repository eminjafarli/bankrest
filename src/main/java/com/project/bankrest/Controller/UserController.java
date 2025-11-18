package com.project.bankrest.Controller;

import com.project.bankrest.Entity.User;
import com.project.bankrest.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(
        name = "Пользователи",
        description = "Операции управления пользователями (регистрация, получение, администрирование)"
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "ADMIN: Получить список пользователей с поиском и пагинацией",
            description = """
                    Возвращает список пользователей.
                    Доступно только администраторам.
                    Поддерживается поиск по имени и email.
                    """,
            parameters = {
                    @Parameter(name = "search", description = "Поиск по имени или email", example = "alex"),
                    @Parameter(name = "page", description = "Номер страницы (0 = первая)", example = "0"),
                    @Parameter(name = "size", description = "Размер страницы", example = "10")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getUsers(search, page, size));
    }

    @Operation(
            summary = "Создать нового пользователя",
            description = """
                    Создает нового пользователя.
                    Используется для публичной регистрации.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные нового пользователя",
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject("""
                                    {
                                      "name": "Alex",
                                      "email": "alex@example.com",
                                      "password": "qwerty123",
                                      "roles": ["USER"]
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.addUser(user));
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его уникальному ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable
            @Parameter(description = "ID пользователя", example = "5")
            Long id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "ADMIN: Получить всех пользователей (без пагинации)",
            description = "Возвращает полный список пользователей. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователи получены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsersForAdmin() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "ADMIN: Обновить данные пользователя",
            description = "Обновляет данные существующего пользователя по его ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Обновляемые данные",
                    content = @Content(
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject("""
                                    {
                                      "name": "Updated Name",
                                      "email": "newemail@example.com",
                                      "roles": ["USER", "ADMIN"]
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable @Parameter(description = "ID пользователя", example = "3") Long id,
            @RequestBody User user
    ) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @Operation(
            summary = "ADMIN: Удалить пользователя",
            description = "Удаляет пользователя по его ID. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Parameter(description = "ID пользователя", example = "7") Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
