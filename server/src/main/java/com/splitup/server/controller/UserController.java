package com.splitup.server.controller;

import com.splitup.model.User;
import com.splitup.server.dto.UserDto;
import com.splitup.service.BusinessException;
import com.splitup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

    /** Busca un usuario por email (login simplificado). */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body("Se requiere el campo 'email'");

        return userService.findByEmail(email)
                .map(u -> ResponseEntity.ok((Object) UserDto.from(u)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe usuario con ese email"));
    }

    /** Registra un nuevo usuario. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name  = body.get("name");
        String email = body.get("email");
        try {
            User user = userService.register(name, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /** Devuelve un usuario por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok((Object) UserDto.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}
