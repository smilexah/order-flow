package sdu.ai.lab.order_service.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}