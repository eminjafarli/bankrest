package com.project.bankrest.Controller;

import com.project.bankrest.DTO.AuthRequest;
import com.project.bankrest.DTO.SignupRequest;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.UserRepository;
import com.project.bankrest.Security.JwtUtils;
import com.project.bankrest.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Авторизация",
        description = "Методы регистрации, аутентификации и управления учетными данными пользователей"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = """
                    Создаёт нового пользователя на основе DTO SignupRequest.
                    Выполняется проверка уникальности username и email.
                    Пароль автоматически кодируется в UserService.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "User registered successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким username или email уже существует",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "User already exists")
                    )
            ),
            @ApiResponse(
                    responseCode = "408",
                    description = "Ошибка выполнения регистрации",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "Unexpected error occurred")
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        try {
            userService.register(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(408).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Аутентификация пользователя",
            description = """
                    Проверяет правильность логина и пароля.
                    В случае успеха возвращает JWT-токен.
                    Токен необходимо использовать в заголовке Authorization:
                    Bearer <token>
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{ \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Неверные учетные данные",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "Invalid credentials")
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtUtils.generateJwtToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(403).body("Invalid credentials");
        }
    }

    @Operation(
            summary = "Регистрация пользователя через сущность User",
            description = """
                    Создаёт пользователя напрямую из Entity-модели.
                    Пароль кодируется вручную в контроллере.
                    Не рекомендуется использовать для клиентских приложений — только технические или админские сценарии.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно создан",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }
}
