package in.gov.indiapost.philately.dto;

import jakarta.validation.constraints.NotNull;

public class CancellationRequestDto {
    @NotNull(message = "Special Cancellation ID is required")
    private Long specialCancellationId;

    private Long itemId;
    private Long orderId;
    private String instructions;

    public CancellationRequestDto() {}

    public Long getSpecialCancellationId() {
        return specialCancellationId;
    }

    public void setSpecialCancellationId(Long specialCancellationId) {
        this.specialCancellationId = specialCancellationId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

