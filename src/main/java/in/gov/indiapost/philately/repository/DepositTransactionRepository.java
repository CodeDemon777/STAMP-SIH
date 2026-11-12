package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.DepositTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {
    List<DepositTransaction> findByAccountOrderByTransactionDateDesc(DepositAccount account);
    Page<DepositTransaction> findByAccountOrderByTransactionDateDesc(DepositAccount account, Pageable pageable);
}

