package sdu.ai.lab.order_service.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckRequest {
    private String productId;
    private Integer quantity;
}
