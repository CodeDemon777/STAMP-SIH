package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.SpecialCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialCancellationRepository extends JpaRepository<SpecialCancellation, Long> {
    List<SpecialCancellation> findByActiveTrueOrderByCancellationDateDesc();
    Optional<SpecialCancellation> findByCancellationCode(String cancellationCode);
}

