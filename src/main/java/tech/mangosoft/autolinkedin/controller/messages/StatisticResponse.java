package tech.mangosoft.autolinkedin.controller.messages;

import org.springframework.beans.factory.annotation.Value;
import tech.mangosoft.autolinkedin.db.entity.CompanyHeadcount;
import tech.mangosoft.autolinkedin.db.entity.Group;
import tech.mangosoft.autolinkedin.db.entity.ProcessingReport;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;

import java.util.Date;
import java.util.HashSet;

public interface StatisticResponse {
    Long getId();
    String getPosition();
    String getFullLocationString();
    String getIndustries();
    String getErrorMessage();
    Task getTask();
    Status getStatus();
    Integer getCountsFound();
    Integer getPage();
    Date getUpdateTime();
    HashSet<CompanyHeadcount> getHeadcounts();
    HashSet<Group> getGroups();

    @Value("#{target.processingReports.get(target.processingReports.size() - 1)}")
    ProcessingReport getLastProcessingReport();

}
