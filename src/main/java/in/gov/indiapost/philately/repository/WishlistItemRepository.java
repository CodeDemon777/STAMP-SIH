package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserOrderByAddedAtDesc(User user);
    Optional<WishlistItem> findByUserAndItem(User user, PhilatelicItem item);
    boolean existsByUserAndItem(User user, PhilatelicItem item);
    void deleteByUserAndItem(User user, PhilatelicItem item);
    long countByUser(User user);
}

