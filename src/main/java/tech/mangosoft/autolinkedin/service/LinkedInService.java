package tech.mangosoft.autolinkedin.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.mangosoft.autolinkedin.controller.messages.*;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;
import tech.mangosoft.autolinkedin.db.repository.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_CONNECTION;
import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_GRABBING;
import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_GRABBING_SALES;

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
    private static final Integer COUNT_DAYS = 10;
    private static final String ID = "id";
    private static List<Predicate> predicates = new ArrayList<>();
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private List<String> names =  null;

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

    @Autowired
    private ICompanyHeadcountRepository companyHeadcountRepository;

    @PersistenceContext
    private EntityManager entityManager;


    public Assignment createGrabbingAssignment(GrabbingMessage message, Account account) {
        Assignment assignment = new Assignment(TASK_GRABBING,
                message.getFullLocationString(),
                message.getPosition(),
                message.getIndustries(),
                account);
        if (checkAllField(assignment)) {
            return null;
        }
        return assignmentRepository.save(assignment.setStatus(Status.STATUS_NEW));
    }

    public Assignment createGrabbingSalesAssignment(GrabbingMessage message, Account account) {
        Assignment assignment = new Assignment(TASK_GRABBING_SALES,
                message.getFullLocationString(),
                message.getPosition(),
                message.getIndustries(),
                account);
        if (checkAllField(assignment)) {
            return null;
        }
        Set<CompanyHeadcount> companyHeadcounts = new HashSet<>();
        for (Long id : message.getCompanyHeadcountsIds()) {
            CompanyHeadcount companyHeadcount = companyHeadcountRepository.getById(id);
            if (companyHeadcount != null) {
                companyHeadcounts.add(companyHeadcount);
            }
        }
        assignment.setHeadcounts(companyHeadcounts);
        return assignmentRepository.save(assignment.setStatus(Status.STATUS_NEW));
    }

    public Assignment createConnectionAssignment(ConnectionMessage message, Account account) {
        Assignment assignment = new Assignment(TASK_CONNECTION,
                message.getFullLocationString(),
                message.getPosition(),
                message.getIndustries(),
                message.getMessage(),
                message.getExecutionLimit(),
                account);
        if (checkMessageAndPosition(assignment)) {
            return null;
        }
//        todo update where bot send message by assignment id
        return assignmentRepository.save(assignment.setStatus(Status.STATUS_NEW));
//        return saveAssignmentAndAddContacts(assignment);
    }


    private Assignment saveAssignmentAndAddContacts(Assignment assignment) {
        assignment.setStatus(Status.STATUS_SUSPENDED);
        Assignment assignmentDB = assignmentRepository.save(assignment);
        List<LinkedInContact> linkedInContact = linkedInContactRepositoryCustom
                .getAllContactsForAssignment(assignmentDB);
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

    public PageImpl<Assignment> getAssignmentByUserAndStatus(Account account, Integer status, Integer count) {
        if (account == null || count == null || status == null) {
            return null;
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Assignment> criteriaQuery = builder.createQuery(Assignment.class);
        Root<Assignment> root = criteriaQuery.from(Assignment.class);
        criteriaQuery.orderBy(builder.desc(root.get(ID)));
        getPredicatesByParam(account, status, root, builder);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<Assignment> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult(0);
        query.setMaxResults(count);

        return new PageImpl<>(query.getResultList(), PageRequest.of(1, count), count);
    }

    /**
     * @param account input account.
     * @param status  assignments.
     * @param root    root object predicates.
     * @param builder CriteriaBuilder object.
     * @author Ichanskiy
     * <p>
     * This is the method get predictes by input param.
     */
    private void getPredicatesByParam(Account account, Integer status, Root<Assignment> root, CriteriaBuilder builder) {
        predicates.clear();
        if (account != null) {
            predicates.add(builder.equal(root.get("account"), account));
        }
        predicates.add(builder.equal(root.get("status"), status));
    }

    /**
     * @param account input account.
     * @param from    date from.
     * @param to      date to.
     * @param root    root object predicates.
     * @param builder CriteriaBuilder object.
     * @author Ichanskiy
     * <p>
     * This is the method get predictes by input param.
     */
    private void getPredicatesByParam(Account account, Integer status, String from, String to,
                                      Root<Assignment> root, CriteriaBuilder builder) {
        predicates.clear();
        if (account != null) {
            predicates.add(builder.equal(root.get("account"), account));
        }
        predicates.add(builder.equal(root.get("status"), status));
        if (!Strings.isEmpty(from) && !Strings.isEmpty(to)) {
            try {
                predicates.add(builder
                        .between(root.get("updateTime"),
                                DateFormat.getDateInstance().parse(from),
                                DateFormat.getDateInstance().parse(to)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param idAssignment id Assignment object.
     * @param idStatus     id Status
     * @author Ichanskiy
     * <p>
     * This is the method change assignment status.
     */
    public void changeStatus(Long idAssignment, Integer idStatus) {
        Assignment assignment = assignmentRepository.getById(idAssignment);
        Status status = getStatusIfExist(idStatus);
        if (status != null) {
            assignment.setStatus(status);
            assignmentRepository.save(assignment);
        }
    }

    /**
     * @param idStatus id Status
     * @return boolean status by id if status found or return null else
     * @author Ichanskiy
     * <p>
     * This is the method get status by id if status found or return null else.
     */
    private Status getStatusIfExist(Integer idStatus) {
        try {
            return Status.getStatusById(idStatus);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param id assignment`s id.
     * @author Ichanskiy
     * <p>
     * This method delete assignment by id
     */
    public void deleteAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        deleteProcessingReportByAssignmentId(assignment);
        deleteAssignmentIdFromContacts(assignment);
        assignmentRepository.delete(assignment);
    }

    /**
     * @param assignment input assignment.
     * @author Ichanskiy
     * <p>
     * This method delete processing report by assignment id
     */
    private void deleteProcessingReportByAssignmentId(Assignment assignment) {
        for (ProcessingReport report : assignment.getProcessingReports()) {
            processingReportRepository.deleteById(report.getId());
        }
    }

    /**
     * @param assignment input assignment.
     * @author Ichanskiy
     * <p>
     * This is method delete assignment id from contacts
     */
    private void deleteAssignmentIdFromContacts(Assignment assignment) {
        List<ContactProcessing> contactProcessings = contactProcessingRepository
                .getAllByAssignmentId(assignment.getId());
        for (ContactProcessing contactProcessing : contactProcessings) {
            contactProcessing.setAssignment(null);
            contactProcessingRepository.save(contactProcessing);
        }
    }

    /**
     * @param assignment input object.
     * @return boolean true if field not null, else false
     * @author Ichanskiy
     * <p>
     * This method chesk fields.
     */
    private boolean checkField(Assignment assignment) {
        if (assignment == null) {
            logger.info("Assignment is null");
            return false;
        }
        if (assignment.getFullLocationString() == null) {
            logger.info("Location or full location is null");
            return false;
        }
        return true;
    }

    /**
     * @param assignment input object.
     * @return boolean true if all field not null, else false
     * @author Ichanskiy
     * <p>
     * This method chesk all fields.
     */
    public boolean checkAllField(Assignment assignment) {
        return assignment == null
                || assignment.getFullLocationString() == null
                || assignment.getFullLocationString().isEmpty()
                || assignment.getIndustries() == null
                || assignment.getIndustries().isEmpty()
                || assignment.getPosition() == null
                || assignment.getPosition().isEmpty();
    }

    /**
     * @param assignment input object.
     * @return boolean true if all field not null, else false
     * @author Ichanskiy
     * <p>
     * This method chesk messege and posiion.
     */
    public boolean checkMessageAndPosition(Assignment assignment) {
        return assignment == null
                || assignment.getMessage() == null
                || assignment.getMessage().isEmpty()
                || assignment.getPosition() == null
                || assignment.getPosition().isEmpty();
    }

    /**
     * @param account current account.
     * @return object that contains statistics
     * @author Ichanskiy
     * <p>
     * This method create statistic.
     */
    public List<StatisticResponse> getStatistics(Account account, Integer page, Integer size) {
        List<StatisticResponse> statisticResponses = new ArrayList<>();
        List<Assignment> assignments = assignmentRepository.findAllByAccount(account,
                PageRequest.of(page - 1, size, Sort.Direction.DESC, ID)).getContent();
        for (Assignment a : assignments) {
            StatisticResponse statistic = new StatisticResponse();
            statistic.setAssignment(a);
            statistic.setCountsFound(a.getCountsFound());
            statistic.setAssignmentName(concatAllString(a.getTask().name(),
                    a.getPosition(),
                    a.getIndustries(),
                    a.getFullLocationString(),
                    a.getCompanyHeadcount() != null ? getHeadcounts(a.getHeadcounts()) : ""));
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

    private String getHeadcounts(Set<CompanyHeadcount> companyHeadcounts){
        String resultString = "";
        for (CompanyHeadcount companyHeadcount : companyHeadcounts) {
            resultString = resultString.concat(companyHeadcount.toString());
        }
        return resultString;
    }

    public StatisticsByDaysMessage getStatisticsByDays(Account account, Date from, Date to) {
        List<Assignment> assignments = assignmentRepository
                .getAllByAccountAndStatusAndUpdateTimeBetween(account, Status.STATUS_FINISHED, from, to);
        return getStatisticsMessageByAssignment(assignments)
                .setAccount(account);
    }

    private StatisticsByDaysMessage getStatisticsMessageByAssignment(List<Assignment> assignments) {
        StatisticsByDaysMessage statistics = new StatisticsByDaysMessage();
        statistics.setConnectedContacts(getConnectedContactsByAssignments(assignments));
        statistics.setGrabbingContacts(getGrabbingContactsByAssignments(assignments));
        return statistics;
    }

    private List<LinkedInContact> getConnectedContactsByAssignments(List<Assignment> assignments) {
        List<LinkedInContact> contacts = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (assignment.getTask().equals(TASK_CONNECTION)) {
                contacts.addAll(getLinkedInContactFromAssignment(assignment));
            }
        }
        return contacts;
    }

    private List<LinkedInContact> getGrabbingContactsByAssignments(List<Assignment> assignments) {
        List<LinkedInContact> contacts = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (assignment.getTask().equals(Task.TASK_GRABBING)) {
                contacts.addAll(getLinkedInContactFromAssignment(assignment));
            }
        }
        return contacts;
    }

    private List<LinkedInContact> getLinkedInContactFromAssignment(Assignment assignment) {
        List<LinkedInContact> contacts = new ArrayList<>();
        List<ContactProcessing> contactProcessings = contactProcessingRepository
                .getAllByAssignmentId(assignment.getId());
        for (ContactProcessing contactProcessing : contactProcessings) {
            contacts.add(contactProcessing.getContact());
        }
        return contacts;
    }

    /**
     * @param account current account.
     * @return object count assignment by account
     * @author Ichanskiy
     * <p>
     * This method get count assignment.
     */
    public Integer getCountAssignment(Account account) {
        return assignmentRepository.countAllByAccount(account);
    }

    /**
     * @param s all strings.
     * @return final string after joining. Example: "SEO; Games; New York;"
     * @author Ichanskiy
     * <p>
     * This method concate all string.
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
     * @param date1 first date.
     * @param date2 second date.
     * @return true if day of date equals, else false.
     * @author Ichanskiy
     * <p>
     * This method who compare date.
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

    private static Date getDateBeforeCountDays(Integer days, int periodLength) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.add(Calendar.DATE, -periodLength);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public StatisticsByConnectionMessage getContactsByConnection(Assignment assignment, Integer page) {
        StatisticsByConnectionMessage statistics = new StatisticsByConnectionMessage();
        statistics.setConnectedContacts(contactRepository.getAllByAssignment(assignment,
                PageRequest.of(page < 0 ? 0 : page - 1, SIZE, Sort.Direction.DESC, ID)));
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

    private boolean assignmentHasThisContact(Assignment assignment, LinkedInContact contact) {
        return assignment.getContacts().contains(contact);
    }

    public PageImpl<Assignment> getAssignmentByParam(AssignmentsByParam m, Account account) {
        if (m.getStatus() == null) {
            return null;
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Assignment> criteriaQuery = builder.createQuery(Assignment.class);
        Root<Assignment> root = criteriaQuery.from(Assignment.class);
        criteriaQuery.orderBy(builder.desc(root.get(ID)));
        getPredicatesByParam(account, m.getStatus(), m.getFrom(), m.getTo(), root, builder);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<Assignment> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult(0);
        query.setMaxResults(SIZE);

        return new PageImpl<>(query.getResultList(), PageRequest.of(1, SIZE), SIZE);
    }
/*
    @Deprecated
    public GraphMessage getGraphByAccount(Account account) {
        GraphMessage graphMessage = new GraphMessage();
        graphMessage.setLabels(getDays());
        graphMessage.setSeries(getValuesByAccount(account));
        return graphMessage;
    }
*/
    public GraphMessage getGraphByType(Account account, String type, int periodLength) {
        GraphMessage graphMessage = new GraphMessage();
        graphMessage.setLabels(getDays(periodLength));
        synchronized (this) {
            names =  new ArrayList<>();
            if (type.equals("links")) {
                graphMessage.setSeries(getValuesByTypeLinks(account, periodLength));
            }
            if (type.equals("messages")) {
                graphMessage.setSeries(getValuesByTypeMessages(account, periodLength));
            }
            if (type.equals("errors")) {
                graphMessage.setSeries(getValuesByTypeErrors(account, periodLength));
            }
            graphMessage.setAccounts(names);
        }
        return graphMessage;
    }

    private List<String> getDays(int periodLength) {
        List<String> sevenDays = new ArrayList<>();
        for (int i = 0; i < periodLength; i++) {
            sevenDays.add(format.format(getDateBeforeCountDays(i, periodLength)));
        }
        return sevenDays;
    }

    private List<String> getAccounts(Account account) {
        List<String> values = new ArrayList<>();
        if (account.isAdmin()) {
            for (Account acc: account.getCompany().getAccounts() ) {
                values.add(acc.getFirst() + " " + acc.getLast());
            }
        } else {
            values.add(account.getFirst() + " " + account.getLast());
        }
        return values;
    }
/*
    private List<Integer[]> getValuesByAccount(Account account) {
        List<Integer[]> values = new ArrayList<>();
        Integer[] countOfErrors = {0, 0, 0, 0, 0, 0, 0};
        Integer[] countOfAddContacts = getCountOfAddContacts(account);
        Integer[] countOfMessages = getCountOfMessages(account);
        values.add(countOfErrors);
        values.add(countOfAddContacts);
        values.add(countOfMessages);
        return values;
    }
*/
    private List<Integer[]> getValuesByTypeLinks(Account account, int periodLength) {
        List<Integer[]> values = new ArrayList<>();
        if (account.isAdmin()) {
            for (Account acc: account.getCompany().getAccounts() ) {
                Integer[] vals = getCountOfAddContacts(acc, periodLength);
                if (isArrayNotEmpty(vals)){
                    names.add(acc.getFirst() + " " + acc.getLast());
                    values.add(vals);
                }

            }
        } else {
            Integer[] vals = getCountOfAddContacts(account, periodLength);
            values.add(vals);
            names.add(account.getFirst() + " " + account.getLast());
        }
        return values;
    }

    private boolean isArrayNotEmpty(Integer[] vals) {
        for (int i=0; i< vals.length; i++){
            if (vals[i]!=0) {
                return true;
            }
        }
        return false;
    }

    private List<Integer[]> getValuesByTypeMessages(Account account, int periodLength) {
        List<Integer[]> values = new ArrayList<>();
        if (account.isAdmin()) {
            for (Account acc: account.getCompany().getAccounts() ) {
                Integer[] vals = getCountOfMessages(acc, periodLength);
                if (isArrayNotEmpty(vals)){
                    names.add(acc.getFirst() + " " + acc.getLast());
                    values.add(vals);
                }
            }
        } else {
            Integer[] vals = getCountOfMessages(account, periodLength);
            values.add(vals);
            names.add(account.getFirst() + " " + account.getLast());
        }
        return values;
    }


    private List<Integer[]> getValuesByTypeErrors(Account account, int periodLength) {
        List<Integer[]> values = new ArrayList<>();
        if (account.isAdmin()) {
            for (Account acc: account.getCompany().getAccounts() ) {
                Integer[] vals =getCountOfErrors(acc, periodLength);
                if (isArrayNotEmpty(vals)){
                    names.add(acc.getFirst() + " " + acc.getLast());
                    values.add(vals);
                }
            }
        } else {
            Integer[] vals = getCountOfErrors(account, periodLength);
            values.add(vals);
            names.add(account.getFirst() + " " + account.getLast());
        }
        return values;
    }

    private Integer[] getCountOfAddContacts(Account account, int periodLength) {
        Integer[] result = new Integer[periodLength];
        for (int i = 0; i < periodLength; i++) {
            result[i] = getLinksCountByDay(account, getDateBeforeCountDays(i, periodLength), getDateBeforeCountDays(i+1, periodLength));
        }
        return result;
    }

    private Integer[] getCountOfMessages(Account account, int periodLength) {
        Integer[] result = new Integer[periodLength];
        for (int i = 0; i < periodLength; i++) {
            result[i] = getMessagesCountByDay(account, getDateBeforeCountDays(i, periodLength), getDateBeforeCountDays(i+1, periodLength));
        }
        return result;
    }

    private Integer[] getCountOfErrors(Account account, int periodLength) {
        Integer[] result = new Integer[periodLength];
        for (int i = 0; i < periodLength; i++) {
            result[i] = getErrorsCountByDay(account, getDateBeforeCountDays(i, periodLength), getDateBeforeCountDays(i+1,periodLength));
        }
        return result;
    }

    /*
    private int getCountAddedContactsByDay(Account account, Date from, Date to) {
        long count = 0;
        List<Assignment> assignments = assignmentRepository
                .getAllByAccountAndTaskAndUpdateTimeBetween(account,
                        Task.TASK_GRABBING,
                        from,
                        to);
        for (Assignment assignment : assignments) {
            for (ProcessingReport report : assignment.getProcessingReports()) {
                if (report.getSaved() != null) {
                    count = count + report.getSaved();
                }
            }
        }
        return (int)count;
    }

    private int getCountMessagesByDay(Account account, Date from, Date to) {
        long count = 0;
        List<Assignment> assignments = assignmentRepository
                .getAllByAccountAndTaskAndUpdateTimeBetween(account,
                        Task.TASK_CONNECTION,
                        from,
                        to);
        for (Assignment assignment : assignments) {
            count += (assignment.getCountMessages() != null) ? assignment.getCountMessages() : 0;
        }
        return (int)count;
    }
*/
    private int getMessagesCountByDay(Account account, Date from, Date to) {
        long count = 0;
        List<ProcessingReport> reports = processingReportRepository
                .getAllByAssignment_AccountAndAssignment_TaskAndUpdateTimeBetween(account,
                        Task.TASK_CONNECTION,
                        from,
                        to);
        for (ProcessingReport report : reports) {
            count += (report.getSaved() != null) ? report.getSaved() : 0;
        }
        return (int)count;
    }

    private int getLinksCountByDay(Account account, Date from, Date to) {
        long count = 0;
        List<ProcessingReport> reports = processingReportRepository
                .getAllByAssignment_AccountAndAssignment_TaskAndUpdateTimeBetween(account,
                        Task.TASK_GRABBING,
                        from,
                        to);
        for (ProcessingReport report : reports) {
            count += (report.getSaved() != null) ? report.getSaved() : 0;
        }
        return (int)count;
    }

    private int getErrorsCountByDay(Account account, Date from, Date to) {
        long count = 0;
        List<ProcessingReport> reports = processingReportRepository
                .getAllByAssignment_AccountAndUpdateTimeBetween(account,
                        from,
                        to);
        for (ProcessingReport report : reports) {
            count += (report.getFailed() != null) ? report.getFailed() : 0;
        }
        return (int)count;
    }
}
