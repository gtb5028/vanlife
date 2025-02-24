package org.beerbower.vanlife.controllers;

import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.UserRepository;
import org.beerbower.vanlife.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@PreAuthorize("permitAll()")
@CrossOrigin
@RequestMapping("/api/login")
public class LoginController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String jwtSecret;
    private final long jwtExpiration ;

    public LoginController(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           @Value("${jwt.secret}") String jwtSecret,
                           @Value("${jwt.expiration}") long jwtExpiration) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    @PostMapping
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        // Find user by email
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new LoginResponseDto(JwtUtils.createJwt(user, jwtSecret, jwtExpiration), user);
    }

    /**
     * Used to receive authentication data (email, password) from the client.
     */
    public record LoginRequestDto(String email, String password) {}

    /**
     * Used to send authentication results (JWT and user details) back to the client.
     */
    public record LoginResponseDto(String jwt, User user) {}
}
