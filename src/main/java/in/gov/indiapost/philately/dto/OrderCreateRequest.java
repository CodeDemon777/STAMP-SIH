package in.gov.indiapost.philately.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class OrderCreateRequest {
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    private String paymentMethod = "PDA_WALLET"; // PDA_WALLET, UPI, DEBIT_CARD
    private String shippingAddress;

    public OrderCreateRequest() {}

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}

