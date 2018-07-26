package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.mangosoft.autolinkedin.controller.messages.StatisticResponse;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import java.util.*;
import java.util.logging.Logger;

/**
 * <h1> LinkedIn Service!</h1>
 * The LinkedInService implements initial logic application
 * <p>
 *
 * Method annotate @Scheduled is point to start do assignment
 * user friendly and it is assumed as a high quality code.
 *
 *
 * @author  Ichanskiy
 * @version 1.0
 * @since   2018-06-06
 */
@Service
public class LinkedInService {

    private static Logger logger = Logger.getLogger(LinkedInService.class.getName());

    @Autowired
    private IAssignmentRepository assignmentRepository;

    /**
     * @author  Ichanskiy
     *
     * This is the method chesk fields.
     * @param assignment input object.
     * @return boolean true if field not null, else false
     */
    private boolean checkField(Assignment assignment){
        if (assignment == null) {
            logger.info("Assignment is null");
            return false;
        }
        if (assignment.getLocation() == null || assignment.getFullLocationString() == null) {
            logger.info("Location or full location is null");
            return false;
        }
        return true;
    }
    /**
     * @author  Ichanskiy
     *
     * This is the method chesk all fields.
     * @param assignment input object.
     * @return boolean true if all field not null, else false
     */
    public boolean checkAllField(Assignment assignment){
        return assignment == null
                || assignment.getFullLocationString() == null
                || assignment.getFullLocationString().isEmpty()
                || assignment.getLocation() == null
                || assignment.getLocation().isEmpty()
                || assignment.getIndustries() == null
                || assignment.getIndustries().isEmpty()
                || assignment.getPosition() == null
                || assignment.getPosition().isEmpty();
    }
    /**
     * @author  Ichanskiy
     *
     * This is the method chesk messege and posiion.
     * @param assignment input object.
     * @return boolean true if all field not null, else false
     */
    public boolean checkMessageAndPosition(Assignment assignment){
        return assignment == null
                || assignment.getMessage() == null
                || assignment.getMessage().isEmpty()
                || assignment.getPosition() == null
                || assignment.getPosition().isEmpty();
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method create statistic.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<StatisticResponse> getStatistics(Account account, Integer page, Integer size) {
        List<StatisticResponse> statisticResponses = new ArrayList<>();
        List<Assignment> assignments = assignmentRepository.findAllByAccount(account, PageRequest.of(page - 1, size,  Sort.Direction.DESC, "id")).getContent();
        for (Assignment a : assignments) {
            StatisticResponse statistic = new StatisticResponse();
            statistic.setAssignmentId(a.getId());
            statistic.setAssignmentName(concatAllString(a.getTask().name(), a.getPosition(), a.getIndustries(), a.getFullLocationString()));
            statistic.setErrorMessage(a.getErrorMessage());
            statistic.setStatus(a.getStatus().name());
            statistic.setPage(a.getPage());
            if (!CollectionUtils.isEmpty(a.getProcessingReports())) {
                int maxIndex = a.getProcessingReports().size() - 1;
                statistic.setProcessed(a.getProcessingReports().get(maxIndex).getProcessed());
                statistic.setSaved(a.getProcessingReports().get(maxIndex).getSaved());
                statistic.setSuccessed(a.getProcessingReports().get(maxIndex).getSuccessed());
                statistic.setFailed(a.getProcessingReports().get(maxIndex).getFailed());
            }
            statisticResponses.add(statistic);
        }
        return statisticResponses;
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get count assignment.
     * @param account current account.
     * @return object count assignment by account
     */
    public Integer getCountAssignment(Account account) {
        return assignmentRepository.countAllByAccount(account);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method concate all string.
     * @param s all strings.
     * @return final string after joining. Example: "SEO; Games; New York;"
     */
    private String concatAllString(String... s) {
        StringBuilder result = new StringBuilder();
        for (String s1 : s) {
            if (s1 == null) {
                continue;
            }
            result.append(s1);
            result.append("; ");
        }
        return result.toString();
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method who compare date.
     * @param date1 first date.
     * @param date2 second date.
     * @return true if day of date equals, else false.
     */
    private static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
