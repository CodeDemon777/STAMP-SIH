package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.PostalCircle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostalCircleRepository extends JpaRepository<PostalCircle, Long> {
    Optional<PostalCircle> findByCode(String code);
}

