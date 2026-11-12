package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.CancellationRequest;
import in.gov.indiapost.philately.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Long> {
    List<CancellationRequest> findByUserOrderByRequestDateDesc(User user);
    List<CancellationRequest> findAllByOrderByRequestDateDesc();
}

