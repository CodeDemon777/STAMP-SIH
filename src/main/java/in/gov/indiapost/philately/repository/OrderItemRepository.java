package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}

