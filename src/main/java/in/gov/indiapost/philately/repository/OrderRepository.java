package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.entity.OrderStatus;
import in.gov.indiapost.philately.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
    long countByUser(User user);
    long countByUserAndOrderStatusIn(User user, List<OrderStatus> statuses);
}

