package sdu.ai.lab.order_service.service;

import sdu.ai.lab.order_service.dto.request.CreateOrderRequest;
import sdu.ai.lab.order_service.dto.request.UpdateOrderStatusRequest;
import sdu.ai.lab.order_service.dto.response.OrderResponse;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByUserId(String userId);

    List<OrderResponse> getOrdersByUserIdAndStatus(String userId, OrderStatus status);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    void cancelOrder(Long id);
}