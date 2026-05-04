package sdu.ai.lab.order_service.entity.enums;

public enum OrderStatus {
    PENDING,     // Создан, ожидает оплаты
    CONFIRMED,   // Оплата прошла
    PROCESSING,  // В обработке на складе
    SHIPPED,     // Отправлен
    DELIVERED,   // Доставлен
    CANCELLED    // Отменён
}