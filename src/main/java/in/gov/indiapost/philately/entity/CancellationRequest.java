package in.gov.indiapost.philately.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_requests")
public class CancellationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "special_cancellation_id", nullable = false)
    private SpecialCancellation specialCancellation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private PhilatelicItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(length = 1000)
    private String instructions;

    @Column(nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, APPROVED, PROCESSED, REJECTED

    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    public CancellationRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SpecialCancellation getSpecialCancellation() {
        return specialCancellation;
    }

    public void setSpecialCancellation(SpecialCancellation specialCancellation) {
        this.specialCancellation = specialCancellation;
    }

    public PhilatelicItem getItem() {
        return item;
    }

    public void setItem(PhilatelicItem item) {
        this.item = item;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
}

