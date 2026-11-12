package in.gov.indiapost.philately.repository;

import in.gov.indiapost.philately.entity.DepositAccount;
import in.gov.indiapost.philately.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {
    Optional<DepositAccount> findByUser(User user);
    Optional<DepositAccount> findByAccountNumber(String accountNumber);
}

