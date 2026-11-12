package in.gov.indiapost.philately;

import in.gov.indiapost.philately.dto.AuthRequest;
import in.gov.indiapost.philately.dto.AuthResponse;
import in.gov.indiapost.philately.dto.OrderCreateRequest;
import in.gov.indiapost.philately.dto.OrderItemRequest;
import in.gov.indiapost.philately.dto.TopUpRequest;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.repository.PhilatelicItemRepository;
import in.gov.indiapost.philately.service.AuthService;
import in.gov.indiapost.philately.service.DepositAccountService;
import in.gov.indiapost.philately.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhilatelyApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private DepositAccountService depositAccountService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PhilatelicItemRepository itemRepository;

    @Test
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(depositAccountService);
        assertNotNull(orderService);
    }

    @Test
    void testAuthentication() {
        AuthRequest loginReq = new AuthRequest("philatelist", "Password123!");
        AuthResponse resp = authService.login(loginReq);
        assertNotNull(resp.getToken());
        assertEquals("philatelist", resp.getUsername());
        assertNotNull(resp.getAccountNumber());
        assertTrue(resp.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testTopUpAndOrderFlow() {
        // Test account summary
        var summary = depositAccountService.getAccountSummary("philatelist");
        assertNotNull(summary.get("balance"));
        BigDecimal initialBalance = (BigDecimal) summary.get("balance");

        // Top up ₹500
        TopUpRequest topUp = new TopUpRequest(BigDecimal.valueOf(500.00), "UPI");
        depositAccountService.topUpBalance("philatelist", topUp);

        DepositAccount accAfter = depositAccountService.getAccountForUser("philatelist");
        assertEquals(initialBalance.add(BigDecimal.valueOf(500.00)), accAfter.getBalance());

        // Place an order using PDA Wallet
        List<PhilatelicItem> items = itemRepository.findAll();
        assertFalse(items.isEmpty());
        PhilatelicItem item = items.get(0);

        OrderCreateRequest orderReq = new OrderCreateRequest();
        orderReq.setPaymentMethod("PDA_WALLET");
        orderReq.setShippingAddress("Speed Post Centre, New Delhi GPO");
        orderReq.setItems(Collections.singletonList(new OrderItemRequest(item.getId(), 2)));

        Order order = orderService.createOrder("philatelist", orderReq);
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals("PAID", order.getPaymentStatus());
        assertNotNull(order.getTrackingNumber());
    }
}

