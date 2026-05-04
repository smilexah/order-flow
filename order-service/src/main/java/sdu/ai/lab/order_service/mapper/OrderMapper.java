package sdu.ai.lab.order_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sdu.ai.lab.order_service.dto.request.CreateOrderRequest;
import sdu.ai.lab.order_service.dto.request.OrderItemRequest;
import sdu.ai.lab.order_service.dto.response.OrderItemResponse;
import sdu.ai.lab.order_service.dto.response.OrderResponse;
import sdu.ai.lab.order_service.entity.Order;
import sdu.ai.lab.order_service.entity.OrderItem;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(CreateOrderRequest request);

    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toItemEntity(OrderItemRequest request);

    @Mapping(target = "subtotal", source = ".", qualifiedByName = "calcSubtotal")
    OrderItemResponse toItemResponse(OrderItem item);

    @Named("calcSubtotal")
    default BigDecimal calcSubtotal(OrderItem item) {
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}