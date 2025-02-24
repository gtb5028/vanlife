package org.beerbower.vanlife.controllers;

import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@PreAuthorize("permitAll()")
@CrossOrigin
@RequestMapping("/api/register")
public class RegistrationController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequestDto request) {

        // Check if email already exists
        Optional<User> existingUserByEmail = userRepository.findByEmail(request.email);
        if (existingUserByEmail.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // Create new user
        User newUser = new User(
                null, request.name, request.email, null,
                passwordEncoder.encode(request.password), "ROLE_USER", true);

        // Save user
        User createdUser = userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Used to receive registration request body (name, email, password) from the client.
     */
    public record RegistrationRequestDto( String name, String email, String password) {}
}
