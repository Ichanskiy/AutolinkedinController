package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.ContactProcessing;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;

import java.util.List;

public interface IContactProcessingRepository extends CrudRepository<ContactProcessing, Long> {

    ContactProcessing findByAccountIdAndContactId(Long accountId, Long contactId);

    Page<ContactProcessing> getDistinctByAccountAndAssignmentAndStatusNot(Account account, Assignment assignment, Integer status, Pageable pageable);

    Integer countDistinctByAccountAndAssignmentAndStatusNot(Account account, Assignment assignment, Integer status);

    Integer countDistinctByAccountAndAssignmentAndStatus(Account account, Assignment assignment, Integer status);

    Page<ContactProcessing> getDistinctByAccountAndAssignmentAndStatus(Account account, Assignment assignment, Integer status, Pageable pageable);

    List<ContactProcessing> getAllByAssignmentId(Long id);
}
