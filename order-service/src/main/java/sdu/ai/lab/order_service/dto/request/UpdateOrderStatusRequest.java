package sdu.ai.lab.order_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}