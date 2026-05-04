package sdu.ai.lab.order_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sdu.ai.lab.order_service.feign.dto.PaymentRequest;
import sdu.ai.lab.order_service.feign.dto.PaymentResponse;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}