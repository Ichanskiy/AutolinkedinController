package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.Account;

import java.util.List;

public interface IAccountRepository extends CrudRepository<Account, Long> {

    Account getById(Long id);

    Account getAccountByUsername(@Param("username") String username);

    Account getAccountByUsernameAndPassword(String username, String password);

    Page<Account> findAll(Pageable pageable);

    List<Account> findAll();

}
