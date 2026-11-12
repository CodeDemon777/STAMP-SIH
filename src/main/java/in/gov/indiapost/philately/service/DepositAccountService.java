package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.dto.TopUpRequest;
import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.DepositTransaction;
import in.gov.indiapost.philately.entity.OrderStatus;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.exception.BadRequestException;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.DepositAccountRepository;
import in.gov.indiapost.philately.repository.DepositTransactionRepository;
import in.gov.indiapost.philately.repository.OrderRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DepositAccountService {

    private final DepositAccountRepository depositAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DepositAccountService(DepositAccountRepository depositAccountRepository,
                                 DepositTransactionRepository depositTransactionRepository,
                                 UserRepository userRepository,
                                 OrderRepository orderRepository) {
        this.depositAccountRepository = depositAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public DepositAccount getAccountForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return depositAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit Account not found for user: " + username));
    }

    public Map<String, Object> getAccountSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        DepositAccount account = depositAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit Account not found"));

        long pendingOrders = orderRepository.countByUserAndOrderStatusIn(
                user,
                Arrays.asList(OrderStatus.ORDER_PLACED, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.IN_TRANSIT)
        );
        long totalOrders = orderRepository.countByUser(user);

        Map<String, Object> summary = new HashMap<>();
        summary.put("accountNumber", account.getAccountNumber());
        summary.put("balance", account.getBalance());
        summary.put("status", account.getStatus());
        summary.put("pendingOrders", pendingOrders);
        summary.put("totalOrders", totalOrders);
        summary.put("standingInstructions", account.getStandingInstructions());
        summary.put("holderName", user.getFullName());
        summary.put("email", user.getEmail());
        summary.put("phone", user.getPhoneNumber());
        return summary;
    }

    @Transactional
    public DepositTransaction topUpBalance(String username, TopUpRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        DepositAccount account = depositAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit Account not found"));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Account is not active. Current status: " + account.getStatus());
        }

        BigDecimal newBalance = account.getBalance().add(req.getAmount());
        account.setBalance(newBalance);
        depositAccountRepository.save(account);

        DepositTransaction txn = new DepositTransaction();
        txn.setAccount(account);
        txn.setTransactionType("CREDIT_TOPUP");
        txn.setAmount(req.getAmount());
        txn.setBalanceAfter(newBalance);
        txn.setReferenceNumber("TOPUP-" + System.currentTimeMillis() + "-" + (100 + new Random().nextInt(900)));
        txn.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "UPI");
        txn.setDescription("Online deposit fund top-up via " + txn.getPaymentMethod());
        txn.setTransactionDate(LocalDateTime.now());

        return depositTransactionRepository.save(txn);
    }

    public List<DepositTransaction> getTransactions(String username) {
        DepositAccount account = getAccountForUser(username);
        return depositTransactionRepository.findByAccountOrderByTransactionDateDesc(account);
    }

    @Transactional
    public DepositAccount updateInstructions(String username, String instructions) {
        DepositAccount account = getAccountForUser(username);
        account.setStandingInstructions(instructions);
        return depositAccountRepository.save(account);
    }
}

