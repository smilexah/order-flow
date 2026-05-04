package sdu.ai.lab.order_service.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super("Payment failed: " + message);
    }
}