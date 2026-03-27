package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        log.info("POST /api/users/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@Valid @RequestBody UserDto.LoginRequest request) {
        log.info("POST /api/users/login");
        return ResponseEntity.ok(userService.login(request));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<UserDto.Response> getUserById(@PathVariable Long id) {
//        return ResponseEntity.ok(userService.getUserById(id));
//    }
@GetMapping("/{id}")
public ResponseEntity<UserDto.Response> getUserById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
}

//    @GetMapping("/email/{email}")
//    public ResponseEntity<UserDto.Response> getUserByEmail(@PathVariable String email) {
//        return ResponseEntity.ok(userService.getUserByEmail(email));
//    }


    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto.Response> getUserByEmail(
            @PathVariable("email") String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<UserDto.Response>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<UserDto.Response> updateUser(
//            @PathVariable Long id,
//            @RequestBody UserDto.UpdateRequest request) {
//        return ResponseEntity.ok(userService.updateUser(id, request));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> updateUser(
            @PathVariable("id") Long id,
            @RequestBody UserDto.UpdateRequest request) {

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }
}
