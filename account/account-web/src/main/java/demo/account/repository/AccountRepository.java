package demo.account.repository;

import demo.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findAccountByEmail(@Param("email") String email);
}
