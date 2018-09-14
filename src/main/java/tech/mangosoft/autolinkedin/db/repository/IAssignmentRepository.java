package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;

import java.util.Date;
import java.util.List;

public interface IAssignmentRepository extends CrudRepository<Assignment, Long> {

    Assignment getById(Long id);

    Assignment getFirstByStatus(Status status);

    List<Assignment> findByStatusOrderById(Status status);

    List<Assignment> getAllByAccountAndStatusAndUpdateTime(Account account, Status status, Date date);

    Page<Assignment> getAllByAccount(Account account, Pageable pageable);

    List<Assignment> getAllByAccount(Account account);

    Page<Assignment> findAllByAccount(Account account, Pageable pageable);

    Integer countAllByAccount(Account account);
}
