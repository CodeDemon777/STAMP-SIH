package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.CancellationRequestDto;
import in.gov.indiapost.philately.entity.CancellationRequest;
import in.gov.indiapost.philately.entity.SpecialCancellation;
import in.gov.indiapost.philately.service.SpecialCancellationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancellations")
public class SpecialCancellationController {

    private final SpecialCancellationService cancellationService;

    public SpecialCancellationController(SpecialCancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecialCancellation>>> getSpecialCancellations() {
        return ResponseEntity.ok(ApiResponse.ok(cancellationService.getActiveCancellations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialCancellation>> getCancellationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cancellationService.getById(id)));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<CancellationRequest>> submitRequest(
            Authentication authentication,
            @Valid @RequestBody CancellationRequestDto dto) {
        CancellationRequest req = cancellationService.submitRequest(authentication.getName(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Special cancellation request submitted successfully", req));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<CancellationRequest>>> getUserRequests(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(cancellationService.getUserRequests(authentication.getName())));
    }
}

