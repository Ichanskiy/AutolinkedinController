package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.repository.IContactProcessingRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;
import tech.mangosoft.autolinkedin.utils.CSVUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
public class ContactService {

    private static Logger logger = Logger.getLogger(ContactService.class.getName());
    private static Integer COUNT_FOR_PAGE = 40;

    private List<Predicate> predicates = new ArrayList<>();

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Value("${storage.path}")
    private String path;

    @Value("${storage.filename}")
    private String filename;


    public PageImpl<LinkedInContact> getContactsByParam(ContactsMessage message){
        if (message == null || message.getPage() == null) {
            return null;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LinkedInContact> criteriaQuery = builder.createQuery(LinkedInContact.class);
        Root<LinkedInContact> root = criteriaQuery.from(LinkedInContact.class);

        getPredicatesByParam(message, root, builder);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<LinkedInContact> query = entityManager.createQuery(criteriaQuery);

        query.setFirstResult((message.getPage() - 1) * COUNT_FOR_PAGE);
        query.setMaxResults(COUNT_FOR_PAGE);

        return new PageImpl<>(query.getResultList(), PageRequest.of(message.getPage(), COUNT_FOR_PAGE), getCountContactsByPredicates(predicates));
    }


    private void getPredicatesByParam(ContactsMessage contactsMessage, Root<LinkedInContact> root, CriteriaBuilder builder) {
        predicates.clear();
        if (contactsMessage.getPosition() != null && !contactsMessage.getPosition().isEmpty()) {
            predicates.add(builder.like(root.get("role"), contactsMessage.getPosition()));
        }
        if (contactsMessage.getLocation() != null && !contactsMessage.getLocation().isEmpty()) {
            Location location = locationRepository.getLocationByLocation(contactsMessage.getLocation());
            if (location != null) {
                predicates.add(builder.equal(root.get("location"), location));
            }
        }
        if (contactsMessage.getIndustries() != null && !contactsMessage.getIndustries().isEmpty()) {
            predicates.add(builder.like(root.get("industries"), contactsMessage.getIndustries()));
        }
    }


    private Long getCountContactsByPredicates(List<Predicate> predicates){
        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(LinkedInContact.class)));
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<LinkedInContact> getProcessedContact(Account account, Assignment assignment, int page, int size) {
        return contactProcessingRepository
                .getDistinctByAccountAndAssignmentAndStatusNot(account, assignment, ContactProcessing.STATUS_ERROR, PageRequest.of(page - 1, size,  Sort.Direction.DESC, "id"))
                .stream()
                .map(contactProcessing -> contactProcessing.getContact().setComments(contactProcessing.getAuditLog()))
                .collect(Collectors.toList());
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get succeed contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<LinkedInContact> getContactsSucceed(Account account, Assignment assignment, int page, int size) {
        return contactProcessingRepository
                .getDistinctByAccountAndAssignmentAndStatusNot(account, assignment, ContactProcessing.STATUS_ERROR, PageRequest.of(page - 1, size,  Sort.Direction.DESC, "id"))
                .stream()
                .map(contactProcessing -> contactProcessing.getContact().setComments(contactProcessing.getAuditLog()))
                .collect(Collectors.toList());
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get saved contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<LinkedInContact> getContactsSaved(Account account, Assignment assignment, int page, int size) {
        return contactProcessingRepository
                .getDistinctByAccountAndAssignmentAndStatusNot(account, assignment, ContactProcessing.STATUS_ERROR, PageRequest.of(page - 1, size,  Sort.Direction.DESC, "id"))
                .stream()
                .map(contactProcessing -> contactProcessing.getContact().setComments(contactProcessing.getAuditLog()))
                .collect(Collectors.toList());
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get count saved contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public Integer getCountSavedContact(Account account, Assignment assignment) {
        return contactProcessingRepository
                .countDistinctByAccountAndAssignmentAndStatusNot(account, assignment, ContactProcessing.STATUS_ERROR);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get count failed contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public Integer getCountFailedContact(Account account, Assignment assignment) {
        return contactProcessingRepository
                .countDistinctByAccountAndAssignmentAndStatus(account, assignment, ContactProcessing.STATUS_ERROR);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get failed contacts.
     * @param account current account.
     * @return object that contains statistics
     */
    public List<LinkedInContact> getContactsFailed(Account account, Assignment assignment, int page, int size) {
        return contactProcessingRepository
                .getDistinctByAccountAndAssignmentAndStatus(account, assignment, ContactProcessing.STATUS_ERROR, PageRequest.of(page - 1, size,  Sort.Direction.DESC, "id"))
                .stream()
                .map(ContactProcessing::getContact)
                .collect(Collectors.toList());
    }


    public LinkedInContact update(LinkedInContact linkedInContactDB, UpdateContactMessage updateContactMessage) {
        Location location = locationRepository.getLocationByLocationLike(updateContactMessage.getLocation());
        if (location == null) {
            locationRepository.save(new Location(updateContactMessage.getLocation()));
        }
        return contactRepository.save(linkedInContactDB.
                setEmail(updateContactMessage.getEmail())
                .setCompanyName(updateContactMessage.getCompanyName())
                .setLastName(updateContactMessage.getLastName())
                .setFirstName(updateContactMessage.getFirstName())
                .setRole(updateContactMessage.getRole())
                .setIndustries(updateContactMessage.getIndustries())
                .setLocation(location)
                .setComments(updateContactMessage.getComment()));
    }

    public void createCsvFile(Location location) throws IOException {
        String csvFile = path.concat(filename);
        File file = new File(csvFile);
        FileWriter writer = new FileWriter(file.getAbsoluteFile());
        CSVUtils.writeLine(writer, Arrays.asList("company name", "first name", "last name", "role", "person linkedin", "location", "email"));
        List<Object[]> resultList = contactRepository.getContactsToCsv(location.getId());
        for (Object[] obj : resultList) {
            CSVUtils.writeLine(writer, Arrays
                    .asList(obj[0] != null ? obj[0].toString().concat(" ").replace(",", ";") : " ",
                            obj[1] != null ? obj[1].toString().concat(" ").replace(",", ";") : " ",
                            obj[2] != null ? obj[2].toString().concat(" ").replace(",", ";") : " ",
                            obj[3] != null ? obj[3].toString().concat(" ").replace(",", ";") : " ",
                            obj[4] != null ? obj[4].toString().concat(" ").replace(",", ";") : " ",
                            obj[5] != null ? obj[5].toString().concat(" ").replace(",", ";") : " ",
                            obj[6] != null ? obj[6].toString().concat(" ").replace(",", ";") : " "));
        }
        writer.flush();
        writer.close();
    }

    private static Date getDate(String s) {
        Date date = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
