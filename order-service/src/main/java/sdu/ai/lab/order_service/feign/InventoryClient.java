package sdu.ai.lab.order_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sdu.ai.lab.order_service.feign.dto.StockCheckRequest;

import java.util.List;

// name совпадает с spring.application.name inventory-service в Eureka
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    // Проверяем наличие товара на складе
    @PostMapping("/check")
    boolean checkStock(@RequestBody StockCheckRequest request);

    // Резервируем товары после подтверждения заказа
    @PostMapping("/reserve")
    void reserveItems(@RequestBody List<StockCheckRequest> items);

    // Освобождаем резерв при отмене заказа
    @PostMapping("/release")
    void releaseItems(@RequestBody List<StockCheckRequest> items);
}