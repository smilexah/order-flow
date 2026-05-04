package sdu.ai.lab.order_service.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productId) {
        super("Insufficient stock for product: " + productId);
    }
}