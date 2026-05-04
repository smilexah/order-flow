package sdu.ai.lab.order_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sdu.ai.lab.order_service.dto.request.CreateOrderRequest;
import sdu.ai.lab.order_service.dto.request.UpdateOrderStatusRequest;
import sdu.ai.lab.order_service.dto.response.ApiResponse;
import sdu.ai.lab.order_service.dto.response.OrderResponse;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;
import sdu.ai.lab.order_service.service.OrderService;

import java.util.List;

@Tag(name = "Orders", description = "Order management API")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @Operation(summary = "Get all orders for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(
            @PathVariable String userId,
            @RequestParam(required = false) OrderStatus status) {

        List<OrderResponse> orders = status != null
                ? orderService.getOrdersByUserIdAndStatus(userId, status)
                : orderService.getOrdersByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @Operation(summary = "Update order status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, request)));
    }

    @Operation(summary = "Cancel an order")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", null));
    }
}