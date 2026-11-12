package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.config.JwtTokenProvider;
import in.gov.indiapost.philately.dto.AuthRequest;
import in.gov.indiapost.philately.dto.AuthResponse;
import in.gov.indiapost.philately.dto.RegisterRequest;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.DepositTransaction;
import in.gov.indiapost.philately.entity.Role;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.exception.BadRequestException;
import in.gov.indiapost.philately.repository.DepositAccountRepository;
import in.gov.indiapost.philately.repository.DepositTransactionRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       DepositAccountRepository depositAccountRepository,
                       DepositTransactionRepository depositTransactionRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username '" + req.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email '" + req.getEmail() + "' is already registered");
        }

        User user = new User();
        user.setUsername(req.getUsername().toLowerCase().trim());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName().trim());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAddressLine1(req.getAddressLine1());
        user.setAddressLine2(req.getAddressLine2());
        user.setCity(req.getCity());
        user.setState(req.getState());
        user.setPincode(req.getPincode());

        if (req.getRole() != null && req.getRole().equalsIgnoreCase("ADMIN")) {
            user.setRole(Role.ROLE_ADMIN);
        } else if (req.getRole() != null && req.getRole().equalsIgnoreCase("OFFICER")) {
            user.setRole(Role.ROLE_OFFICER);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        User savedUser = userRepository.save(user);

        // Create National Philately Deposit Account (PDA)
        DepositAccount pda = new DepositAccount();
        pda.setUser(savedUser);
        pda.setAccountNumber("PDA-" + (100000 + new Random().nextInt(900000)));
        pda.setBalance(BigDecimal.valueOf(1500.00)); // Default introductory credit
        pda.setStatus("ACTIVE");
        DepositAccount savedPda = depositAccountRepository.save(pda);

        // Record Initial Opening Balance Deposit Transaction
        DepositTransaction initTxn = new DepositTransaction();
        initTxn.setAccount(savedPda);
        initTxn.setTransactionType("CREDIT_TOPUP");
        initTxn.setAmount(BigDecimal.valueOf(1500.00));
        initTxn.setBalanceAfter(BigDecimal.valueOf(1500.00));
        initTxn.setReferenceNumber("INIT-" + System.currentTimeMillis());
        initTxn.setPaymentMethod("SYSTEM_BONUS");
        initTxn.setDescription("Initial Philatelic Deposit Account welcome credit");
        initTxn.setTransactionDate(LocalDateTime.now());
        depositTransactionRepository.save(initTxn);

        // Authenticate automatically
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                savedPda.getAccountNumber(),
                savedPda.getBalance()
        );
    }

    public AuthResponse login(AuthRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(req.getUsername())
                .or(() -> userRepository.findByEmail(req.getUsername()))
                .orElseThrow(() -> new BadRequestException("User not found"));

        DepositAccount pda = depositAccountRepository.findByUser(user).orElse(null);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                pda != null ? pda.getAccountNumber() : "N/A",
                pda != null ? pda.getBalance() : BigDecimal.ZERO
        );
    }
}

