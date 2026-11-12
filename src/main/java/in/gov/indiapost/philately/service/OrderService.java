package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.dto.OrderCreateRequest;
import in.gov.indiapost.philately.dto.OrderItemRequest;
import in.gov.indiapost.philately.entity.*;
import in.gov.indiapost.philately.exception.BadRequestException;
import in.gov.indiapost.philately.exception.InsufficientBalanceException;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final PhilatelicItemRepository itemRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        PhilatelicItemRepository itemRepository,
                        DepositAccountRepository depositAccountRepository,
                        DepositTransactionRepository depositTransactionRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
    }

    @Transactional
    public Order createOrder(String username, OrderCreateRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Validate stock and calculate total
        for (OrderItemRequest itemReq : req.getItems()) {
            PhilatelicItem item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemReq.getItemId()));

            if (item.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + item.getTitle() + "'. Available: " + item.getStockQuantity());
            }

            // Decrement stock
            item.setStockQuantity(item.getStockQuantity() - itemReq.getQuantity());
            itemRepository.save(item);

            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setUnitPrice(item.getPrice());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
        }

        // Handle PDA Payment Deduction
        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod().toUpperCase() : "PDA_WALLET";
        if ("PDA_WALLET".equalsIgnoreCase(paymentMethod)) {
            DepositAccount account = depositAccountRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("National Philately Deposit Account not found for user"));

            if (account.getBalance().compareTo(totalAmount) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient PDA balance. Required: ₹" + totalAmount + ", Available: ₹" + account.getBalance() +
                        ". Please top up your National Philately Deposit Account."
                );
            }

            BigDecimal newBalance = account.getBalance().subtract(totalAmount);
            account.setBalance(newBalance);
            depositAccountRepository.save(account);

            DepositTransaction txn = new DepositTransaction();
            txn.setAccount(account);
            txn.setTransactionType("DEBIT_PURCHASE");
            txn.setAmount(totalAmount);
            txn.setBalanceAfter(newBalance);
            txn.setReferenceNumber("ORD-DEBIT-" + System.currentTimeMillis());
            txn.setPaymentMethod("PDA_WALLET");
            txn.setDescription("Deduction for Philatelic Order");
            txn.setTransactionDate(LocalDateTime.now());
            depositTransactionRepository.save(txn);
        }

        // Create Order
        Order order = new Order();
        String orderNumber = "INP" + (1000000 + new Random().nextInt(9000000));
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PAID");
        order.setOrderStatus(OrderStatus.ORDER_PLACED);
        order.setTrackingNumber("SP" + (10000000 + new Random().nextInt(90000000)) + "IN");
        
        String address = req.getShippingAddress();
        if (address == null || address.isBlank()) {
            address = String.format("%s, %s, %s, %s - %s",
                    user.getAddressLine1() != null ? user.getAddressLine1() : "India Post HQ",
                    user.getAddressLine2() != null ? user.getAddressLine2() : "",
                    user.getCity() != null ? user.getCity() : "New Delhi",
                    user.getState() != null ? user.getState() : "Delhi",
                    user.getPincode() != null ? user.getPincode() : "110001"
            );
        }
        order.setShippingAddress(address);
        order.setExpectedDeliveryDate(LocalDate.now().plusDays(5));

        Order savedOrder = orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
            orderItemRepository.save(orderItem);
        }

        savedOrder.setItems(orderItems);
        return savedOrder;
    }

    public List<Order> getUserOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderDetails(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}

