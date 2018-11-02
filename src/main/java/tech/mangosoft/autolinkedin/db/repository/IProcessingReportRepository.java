package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.repository.CrudRepository;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.ProcessingReport;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;

import java.util.Date;
import java.util.List;

public interface IProcessingReportRepository extends CrudRepository<ProcessingReport, Long> {

    ProcessingReport getById(Long id);

    List<ProcessingReport> getAllByAssignment_AccountAndAssignment_TaskAndUpdateTimeBetween(Account account,
                                                                                            Task task,
                                                                                            Date from,
                                                                                            Date to);

    List<ProcessingReport> getAllByAssignment_AccountAndAssignment_TaskNotAndUpdateTimeBetween(Account account,
                                                                                               Task task,
                                                                                               Date from,
                                                                                               Date to);

    List<ProcessingReport> getAllByAssignment_AccountAndUpdateTimeBetween(Account account,
                                                                          Date from,
                                                                          Date to);

}
