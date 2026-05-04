package sdu.ai.lab.order_service.feign.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String transactionId;
    private String status; // SUCCESS, FAILED
    private String message;
}