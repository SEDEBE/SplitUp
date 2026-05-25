package com.splitup.server.controller;

import com.splitup.model.User;
import com.splitup.server.dto.UserDto;
import com.splitup.service.BusinessException;
import com.splitup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body("Se requiere el campo 'email'");

        // Login con contraseña
        if (password != null && !password.isBlank()) {
            Optional<User> found = userService.loginWithPassword(email, password);
            return found
                    .map(u -> ResponseEntity.ok((Object) UserDto.from(u)))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Email o contraseña incorrectos"));
        }

        // Fallback: login sin contraseña (compatibilidad)
        return userService.findByEmail(email)
                .map(u -> ResponseEntity.ok((Object) UserDto.from(u)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe usuario con ese email"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name     = body.get("name");
        String email    = body.get("email");
        String password = body.get("password");

        try {
            User user;
            if (password != null && !password.isBlank()) {
                user = userService.registerWithPassword(name, email, password);
            } else {
                user = userService.register(name, email);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> body) {
        // El frontend ya verificó el idToken con Google; aquí solo registramos/buscamos el usuario
        String email = body.get("email");
        String name  = body.get("name");

        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body("Se requiere el campo 'email'");

        Optional<User> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            return ResponseEntity.ok(UserDto.from(existing.get()));
        }

        // Primera vez con Google — registrar automáticamente
        try {
            User user = userService.register(name != null ? name : email, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok((Object) UserDto.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}
