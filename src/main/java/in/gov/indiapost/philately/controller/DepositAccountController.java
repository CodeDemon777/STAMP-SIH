package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.TopUpRequest;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.DepositTransaction;
import in.gov.indiapost.philately.service.DepositAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class DepositAccountController {

    private final DepositAccountService depositAccountService;

    public DepositAccountController(DepositAccountService depositAccountService) {
        this.depositAccountService = depositAccountService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccountSummary(Authentication authentication) {
        Map<String, Object> summary = depositAccountService.getAccountSummary(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/top-up")
    public ResponseEntity<ApiResponse<DepositTransaction>> topUpBalance(
            Authentication authentication,
            @Valid @RequestBody TopUpRequest req) {
        DepositTransaction txn = depositAccountService.topUpBalance(authentication.getName(), req);
        return ResponseEntity.ok(ApiResponse.ok("Funds added successfully to your Philatelic Deposit Account!", txn));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<DepositTransaction>>> getTransactions(Authentication authentication) {
        List<DepositTransaction> txns = depositAccountService.getTransactions(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(txns));
    }

    @PutMapping("/instructions")
    public ResponseEntity<ApiResponse<DepositAccount>> updateInstructions(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        String instructions = body.get("instructions");
        DepositAccount updated = depositAccountService.updateInstructions(authentication.getName(), instructions);
        return ResponseEntity.ok(ApiResponse.ok("Standing instructions updated successfully", updated));
    }
}

