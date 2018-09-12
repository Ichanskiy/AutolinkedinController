package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.mangosoft.autolinkedin.controller.messages.*;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;
import tech.mangosoft.autolinkedin.db.repository.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_CONNECTION;
import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_GRABBING;

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

    private static final Logger logger = Logger.getLogger(LinkedInService.class.getName());
    private static final Integer SIZE = 50;
    private static final String ID = "id";
    private static Logger logger = Logger.getLogger(LinkedInService.class.getName());

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private IProcessingReportRepository processingReportRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private LinkedInContactRepositoryCustomImpl linkedInContactRepositoryCustom;


    public Assignment createGrabbingAssignment(GrabbingMessage message, Account account){
        Assignment assignment = new Assignment(TASK_GRABBING,
                message.getLocation(),
                message.getFullLocationString(),
                message.getPosition(),
                message.getIndustries(),
                account);
        if (checkAllField(assignment)) {
            return null;
        }
        return assignmentRepository.save(assignment);
    }

    public Assignment createConnectionAssignment(ConnectionMessage message, Account account){
        Assignment assignment = new Assignment(TASK_CONNECTION,
                message.getLocation(),
                message.getFullLocationString(),
                message.getPosition(),
                message.getIndustries(),
                message.getMessage(),
                message.getExecutionLimit(),
                account);
        if (checkMessageAndPosition(assignment)) {
            return null;
        }
        return saveAssignmentAndAddContacts(assignment);
    }


    private Assignment saveAssignmentAndAddContacts(Assignment assignment) {
        assignment.setStatus(Status.STATUS_SUSPENDED);
        Assignment assignmentDB = assignmentRepository.save(assignment);
        List<LinkedInContact> linkedInContact = linkedInContactRepositoryCustom.getAllContactsForAssignment(assignmentDB);
        if (linkedInContact != null) {
            setAssignmentToContacts(assignmentDB, linkedInContact);
        }
        return assignmentDB;
    }

    private void setAssignmentToContacts(Assignment assignment, List<LinkedInContact> linkedInContact) {
        for (LinkedInContact contact : linkedInContact) {
            contact.setAssignment(assignment);
            contactRepository.save(contact);
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method change assignment status.
     * @param idAssignment id Assignment object.
     * @param idStatus id Status
     */
    public void changeStatus(Long idAssignment, Integer idStatus){
        Assignment assignment = assignmentRepository.getById(idAssignment);
        Status status = getStatusIfExist(idStatus);
        if (status != null) {
            assignment.setStatus(status);
            assignmentRepository.save(assignment);
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get status by id if status found or return null else.
     * @param idStatus id Status
     * @return boolean status by id if status found or return null else
     */
    private Status getStatusIfExist(Integer idStatus) {
        try {
            return Status.getStatusById(idStatus);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This method delete assignment by id
     * @param id assignment`s id.
     */
    public void deleteAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        deleteProcessingReportByAssignmentId(assignment);
        deleteAssignmentIdFromContacts(assignment);
        assignmentRepository.delete(assignment);
    }

    /**
     * @author  Ichanskiy
     *
     * This method delete processing report by assignment id
     * @param assignment input assignment.
     */
    private void deleteProcessingReportByAssignmentId(Assignment assignment) {
        for (ProcessingReport report : assignment.getProcessingReports()) {
            processingReportRepository.deleteById(report.getId());
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This is method delete assignment id from contacts
     * @param assignment input assignment.
     */
    private void deleteAssignmentIdFromContacts(Assignment assignment) {
        List<ContactProcessing> contactProcessings = contactProcessingRepository.getAllByAssignmentId(assignment.getId());
        for (ContactProcessing contactProcessing : contactProcessings) {
            contactProcessing.setAssignment(null);
            contactProcessingRepository.save(contactProcessing);
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This method chesk fields.
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
     * This method chesk all fields.
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
     * This method chesk messege and posiion.
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
     * This method create statistic.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<StatisticResponse> getStatistics(Account account, Integer page, Integer size) {
        List<StatisticResponse> statisticResponses = new ArrayList<>();
        List<Assignment> assignments = assignmentRepository.findAllByAccount(account, PageRequest.of(page - 1, size, Sort.Direction.DESC, "id")).getContent();
        for (Assignment a : assignments) {
            StatisticResponse statistic = new StatisticResponse();
            statistic.setAssignment(a);
            statistic.setCountsFound(a.getCountsFound());
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

    public StatisticsByTwoDaysMessage getStatisticsByTwoDays(Account account) {
        List<Assignment> assignments = assignmentRepository.getAllByAccountAndStatusAndUpdateTime(account, Status.STATUS_FINISHED, getBeforeYesterday());
        return getStatisticsByTwoDaysMessageByAssignment(assignments)
                .setAccount(account);
    }

    private StatisticsByTwoDaysMessage getStatisticsByTwoDaysMessageByAssignment(List<Assignment> assignments){
        StatisticsByTwoDaysMessage statistics = new StatisticsByTwoDaysMessage();
        statistics.setConnectedContacts(getConnectedContactsByAssignments(assignments));
        statistics.setGrabbingContacts(getGrabbingContactsByAssignments(assignments));
        return statistics;
    }

    private List<LinkedInContact> getConnectedContactsByAssignments(List<Assignment> assignments) {
        List<LinkedInContact> contacts = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (assignment.getTask().equals(TASK_CONNECTION)){
                contacts.addAll(getLinkedInContactFromAssignment(assignment));
            }
        }
        return contacts;
    }

    private List<LinkedInContact> getGrabbingContactsByAssignments(List<Assignment> assignments) {
        List<LinkedInContact> contacts = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (assignment.getTask().equals(Task.TASK_GRABBING)){
                contacts.addAll(getLinkedInContactFromAssignment(assignment));
            }
        }
        return contacts;
    }

    private List<LinkedInContact> getLinkedInContactFromAssignment(Assignment assignment){
        List<LinkedInContact> contacts = new ArrayList<>();
        List<ContactProcessing> contactProcessings = contactProcessingRepository.getAllByAssignmentId(assignment.getId());
        for (ContactProcessing contactProcessing : contactProcessings) {
            contacts.add(contactProcessing.getContact());
        }
        return contacts;
    }

    /**
     * @author  Ichanskiy
     *
     * This method get count assignment.
     * @param account current account.
     * @return object count assignment by account
     */
    public Integer getCountAssignment(Account account) {
        return assignmentRepository.countAllByAccount(account);
    }

    /**
     * @author  Ichanskiy
     *
     * This method concate all string.
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
     * This method who compare date.
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

    private static Date getBeforeYesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        return cal.getTime();
    }

    public StatisticsByConnectionMessage getContactsByConnection(Assignment assignment, Integer page) {
        StatisticsByConnectionMessage statistics = new StatisticsByConnectionMessage();
        statistics.setConnectedContacts(contactRepository.getAllByAssignment(assignment, PageRequest.of(page < 0 ? 0 : page - 1, SIZE,  Sort.Direction.DESC, ID)));
        statistics.setAssignment(assignment);
        return statistics;
    }

    public void deleteContactsFromAssignment(Assignment assignment, List<Long> contactsIds) {
        List<LinkedInContact> contacts = contactRepository.findAllById(contactsIds);
        for (LinkedInContact contact : contacts) {
            if (assignmentHasThisContact(assignment, contact)) {
                contact.setAssignment(null);
                assignment.removeLinkedInContact(contact);
                contactRepository.save(contact);
            }
        }
        assignmentRepository.save(assignment);
    }

    private boolean assignmentHasThisContact(Assignment assignment, LinkedInContact contact){
        return assignment.getContacts().contains(contact);
    }
}
