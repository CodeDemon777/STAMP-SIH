package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.dto.OrderCreateRequest;
import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderCreateRequest req) {
        Order order = orderService.createOrder(authentication.getName(), req);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully via " + order.getPaymentMethod(), order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(Authentication authentication) {
        List<Order> orders = orderService.getUserOrders(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(@PathVariable String orderNumber) {
        Order order = orderService.getOrderDetails(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<Order>> trackOrder(@PathVariable String trackingNumber) {
        Order order = orderService.getOrderDetails(trackingNumber);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }
}

