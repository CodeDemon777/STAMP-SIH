package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.UserProfileDto;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.DepositAccountRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final DepositAccountRepository depositAccountRepository;

    public UserController(UserRepository userRepository, DepositAccountRepository depositAccountRepository) {
        this.userRepository = userRepository;
        this.depositAccountRepository = depositAccountRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<User>> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileDto dto) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddressLine1() != null) user.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) user.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getState() != null) user.setState(dto.getState());
        if (dto.getPincode() != null) user.setPincode(dto.getPincode());

        if (dto.getStandingInstructions() != null) {
            DepositAccount account = depositAccountRepository.findByUser(user).orElse(null);
            if (account != null) {
                account.setStandingInstructions(dto.getStandingInstructions());
                depositAccountRepository.save(account);
            }
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updatedUser));
    }
}

