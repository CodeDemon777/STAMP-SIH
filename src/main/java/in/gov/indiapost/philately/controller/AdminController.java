package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.ItemDto;
import in.gov.indiapost.philately.entity.CancellationRequest;
import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.entity.OrderStatus;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.repository.DepositAccountRepository;
import in.gov.indiapost.philately.repository.OrderRepository;
import in.gov.indiapost.philately.repository.PhilatelicItemRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import in.gov.indiapost.philately.service.OrderService;
import in.gov.indiapost.philately.service.PhilatelicItemService;
import in.gov.indiapost.philately.service.SpecialCancellationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
public class AdminController {

    private final PhilatelicItemService itemService;
    private final OrderService orderService;
    private final SpecialCancellationService cancellationService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PhilatelicItemRepository itemRepository;
    private final DepositAccountRepository depositAccountRepository;

    public AdminController(PhilatelicItemService itemService,
                           OrderService orderService,
                           SpecialCancellationService cancellationService,
                           UserRepository userRepository,
                           OrderRepository orderRepository,
                           PhilatelicItemRepository itemRepository,
                           DepositAccountRepository depositAccountRepository) {
        this.itemService = itemService;
        this.orderService = orderService;
        this.cancellationService = cancellationService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.depositAccountRepository = depositAccountRepository;
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", userRepository.count());
        metrics.put("totalOrders", orderRepository.count());
        metrics.put("totalItems", itemRepository.count());
        metrics.put("totalAccounts", depositAccountRepository.count());
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<PhilatelicItem>> createItem(@Valid @RequestBody ItemDto dto) {
        PhilatelicItem created = itemService.createItem(dto);
        return ResponseEntity.ok(ApiResponse.ok("Philatelic item created successfully", created));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<PhilatelicItem>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDto dto) {
        PhilatelicItem updated = itemService.updateItem(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Philatelic item updated successfully", updated));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted successfully", null));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status").toUpperCase());
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated to " + status, updated));
    }

    @GetMapping("/cancellations/requests")
    public ResponseEntity<ApiResponse<List<CancellationRequest>>> getAllCancellationRequests() {
        return ResponseEntity.ok(ApiResponse.ok(cancellationService.getAllRequests()));
    }

    @PutMapping("/cancellations/requests/{id}/status")
    public ResponseEntity<ApiResponse<CancellationRequest>> updateCancellationRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        CancellationRequest updated = cancellationService.updateRequestStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Request status updated to " + status, updated));
    }
}

