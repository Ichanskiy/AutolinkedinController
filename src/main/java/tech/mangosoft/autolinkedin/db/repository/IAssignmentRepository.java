package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;

import java.util.Date;
import java.util.List;

public interface IAssignmentRepository extends CrudRepository<Assignment, Long> {

    Assignment getById(Long id);

    Assignment getFirstByStatus(Status status);

    List<Assignment> findByStatusOrderById(Status status);

    List<Assignment> getAllByAccountAndTaskAndStatusAndUpdateTimeBetween(Account account,
                                                                         Task task,
                                                                         Status status,
                                                                         Date from,
                                                                         Date to);
    List<Assignment> getAllByAccountAndTaskAndUpdateTimeBetween(Account account,
                                                                         Task task,
                                                                         Date from,
                                                                         Date to);

    @Query("select distinct a.industries from Assignment a where a.industries is not null")
    List<String> getAllIndustries();

    Page<Assignment> getAllByAccount(Account account, Pageable pageable);

    List<Assignment> getAllByAccount(Account account);

    Page<Assignment> findAllByAccount(Account account, Pageable pageable);

    Integer countAllByAccount(Account account);

    List<Assignment> getAllByUpdateTimeBetween(Date from, Date to);

    List<Assignment> getAllByAccountAndStatusAndUpdateTimeBetween(Account account, Status status, Date from, Date to);

    List<Assignment> getAllByAccountAndTaskAndStatus(Account account, Task taskGrabbing, Status status);
}
