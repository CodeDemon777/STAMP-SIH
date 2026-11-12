package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.AuthRequest;
import in.gov.indiapost.philately.dto.AuthResponse;
import in.gov.indiapost.philately.dto.RegisterRequest;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.repository.DepositAccountRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import in.gov.indiapost.philately.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final DepositAccountRepository depositAccountRepository;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          DepositAccountRepository depositAccountRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.depositAccountRepository = depositAccountRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful. National Philately Deposit Account activated!", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
        }
        DepositAccount pda = depositAccountRepository.findByUser(user).orElse(null);

        AuthResponse resp = new AuthResponse(
                null,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                pda != null ? pda.getAccountNumber() : "N/A",
                pda != null ? pda.getBalance() : BigDecimal.ZERO
        );
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }
}

