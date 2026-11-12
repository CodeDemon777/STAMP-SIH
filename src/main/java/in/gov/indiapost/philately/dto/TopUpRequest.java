package in.gov.indiapost.philately.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TopUpRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "50.00", message = "Minimum top-up amount is ₹50.00")
    private BigDecimal amount;

    private String paymentMethod = "UPI"; // UPI, NET_BANKING, DEBIT_CARD

    public TopUpRequest() {}

    public TopUpRequest(BigDecimal amount, String paymentMethod) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

