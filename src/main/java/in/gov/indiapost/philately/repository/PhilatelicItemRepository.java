package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.MaterialType;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.PostalCircle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhilatelicItemRepository extends JpaRepository<PhilatelicItem, Long> {

    Optional<PhilatelicItem> findByItemCode(String itemCode);

    List<PhilatelicItem> findByFeaturedTrue();

    List<PhilatelicItem> findByPopularTrue();

    @Query("SELECT p FROM PhilatelicItem p WHERE " +
           "(:circleId IS NULL OR p.postalCircle.id = :circleId) AND " +
           "(:materialType IS NULL OR p.materialType = :materialType) AND " +
           "(:releaseYear IS NULL OR p.releaseYear = :releaseYear) AND " +
           "(:theme IS NULL OR LOWER(p.theme) = LOWER(:theme)) AND " +
           "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<PhilatelicItem> findWithFilters(
            @Param("circleId") Long circleId,
            @Param("materialType") MaterialType materialType,
            @Param("releaseYear") Integer releaseYear,
            @Param("theme") String theme,
            @Param("query") String query,
            Pageable pageable
    );

    List<PhilatelicItem> findTop4ByThemeAndIdNot(String theme, Long id);
}

