package sdu.ai.lab.order_service.dto.response;

import lombok.Data;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}