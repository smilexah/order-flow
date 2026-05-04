package sdu.ai.lab.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sdu.ai.lab.order_service.entity.Order;
import sdu.ai.lab.order_service.entity.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);
}
