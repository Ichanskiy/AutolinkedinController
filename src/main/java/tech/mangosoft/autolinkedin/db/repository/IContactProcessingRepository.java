package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.repository.CrudRepository;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.ContactProcessing;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;

import java.util.List;

public interface IContactProcessingRepository extends CrudRepository<ContactProcessing, Long> {

    ContactProcessing findByAccountIdAndContactId(Long accountId, Long contactId);

    List<ContactProcessing> getAllByAccountAndAssignmentAndStatusNot(Account account, Assignment assignment, Integer status);

    List<ContactProcessing> getAllByAccountAndAssignmentAndStatus(Account account, Assignment assignment, Integer status);
}
