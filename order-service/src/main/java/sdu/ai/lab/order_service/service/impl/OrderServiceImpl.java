package sdu.ai.lab.order_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sdu.ai.lab.order_service.dto.request.CreateOrderRequest;
import sdu.ai.lab.order_service.dto.request.UpdateOrderStatusRequest;
import sdu.ai.lab.order_service.dto.response.OrderResponse;
import sdu.ai.lab.order_service.entity.Order;
import sdu.ai.lab.order_service.entity.OrderItem;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;
import sdu.ai.lab.order_service.exception.InsufficientStockException;
import sdu.ai.lab.order_service.exception.OrderNotFoundException;
import sdu.ai.lab.order_service.exception.PaymentFailedException;
import sdu.ai.lab.order_service.feign.InventoryClient;
import sdu.ai.lab.order_service.feign.PaymentClient;
import sdu.ai.lab.order_service.feign.dto.PaymentRequest;
import sdu.ai.lab.order_service.feign.dto.PaymentResponse;
import sdu.ai.lab.order_service.feign.dto.StockCheckRequest;
import sdu.ai.lab.order_service.mapper.OrderMapper;
import sdu.ai.lab.order_service.repository.OrderRepository;
import sdu.ai.lab.order_service.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // 1. Проверяем наличие каждого товара в inventory-service
        request.getItems().forEach(item -> {
            boolean inStock = inventoryClient.checkStock(
                    new StockCheckRequest(item.getProductId(), item.getQuantity())
            );
            if (!inStock) {
                throw new InsufficientStockException(item.getProductId());
            }
        });

        // 2. Собираем сущность заказа
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> {
                    OrderItem item = orderMapper.toItemEntity(itemReq);
                    item.setOrder(order);
                    return item;
                })
                .toList();

        order.setItems(items);

        // 3. Считаем итоговую сумму
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        // 4. Обрабатываем платёж через payment-service
        PaymentResponse paymentResponse = paymentClient.processPayment(
                new PaymentRequest(savedOrder.getId(), request.getUserId(), total)
        );

        if (!"SUCCESS".equals(paymentResponse.getStatus())) {
            // Откатываем статус заказа при неудаче оплаты
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new PaymentFailedException(paymentResponse.getMessage());
        }

        // 5. Резервируем товары на складе
        List<StockCheckRequest> reserveRequests = items.stream()
                .map(i -> new StockCheckRequest(i.getProductId(), i.getQuantity()))
                .toList();
        inventoryClient.reserveItems(reserveRequests);

        // 6. Подтверждаем заказ
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(savedOrder);

        log.info("Order {} created and confirmed for user: {}", confirmedOrder.getId(), request.getUserId());
        return orderMapper.toResponse(confirmedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUserIdAndStatus(String userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = findOrderById(id);

        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findOrderById(id);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        // Освобождаем резерв на складе если заказ был подтверждён
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PROCESSING) {
            List<StockCheckRequest> releaseRequests = order.getItems().stream()
                    .map(i -> new StockCheckRequest(i.getProductId(), i.getQuantity()))
                    .toList();
            inventoryClient.releaseItems(releaseRequests);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", id);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    // Запрещаем неправильные переходы статусов (например, DELIVERED -> PENDING)
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", current, next)
            );
        }
    }
}