package tech.mangosoft.autolinkedin.service;

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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static tech.mangosoft.autolinkedin.utils.CSVUtils.parseLine;

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

    private static final Integer COUNT_FOR_PAGE = 40;
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private Integer FIRST_NAME_POSITION = 1;
    private Integer LAST_NAME_POSITION = 2;
    private Integer EMAIL_NAME_POSITION = -1;
    private Integer ID_POSITION = -1;

    private List<Predicate> predicates = new ArrayList<>();

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${storage.path}")
    private String path;

    @Value("${storage.filename}")
    private String filename;


    public void createCsvFileByParam(ContactsMessage message) throws IOException {
        List<LinkedInContact> contacts = getContactsByParamWithoutBound(message);
        writeToCSVFile(contacts);
    }

    private List<LinkedInContact> getContactsByParamWithoutBound(ContactsMessage message) {
        if (message == null || message.getPage() == null) {
            return null;
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LinkedInContact> criteriaQuery = builder.createQuery(LinkedInContact.class);
        Root<LinkedInContact> root = criteriaQuery.from(LinkedInContact.class);
        getPredicatesByParam(message, root, builder);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<LinkedInContact> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    private void writeToCSVFile(final List<LinkedInContact> contactsFromDb) throws IOException {
        String csvFile = path.concat(filename);
        File file = new File(csvFile);
        FileWriter writer = new FileWriter(file.getAbsoluteFile());
        CSVUtils.writeLine(writer, Arrays.asList("id", "company_name", "company_website", "first_name", "last_name", "role", "person_linkedin", "location", "industries", "email"));
        for (LinkedInContact contact : contactsFromDb) {
            if (!isNotNullOrEmpty(contact.getFirstName(), contact.getLastName())) {
                continue;
            }
            CSVUtils.writeLine(writer, Arrays
                    .asList(contact.getId() != null ? contact.getId().toString().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getCompanyName()) ? contact.getCompanyName().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getCompanyWebsite()) ? contact.getCompanyWebsite().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getFirstName()) ? contact.getFirstName().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getLastName()) ? contact.getLastName().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getRole()) ? contact.getRole().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getLinkedin()) ? contact.getLinkedin().concat(" ").replace(",", ";") : " ",
                            contact.getLocation() != null ? contact.getLocation().getLocation().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getIndustries()) ? contact.getIndustries().concat(" ").replace(",", ";") : " ",
                            isNotNullOrEmpty(contact.getEmail()) ? contact.getEmail().concat(" ").replace(",", ";") : " "));
        }
        writer.flush();
        writer.close();
    }

    public boolean exportCSVFilesToDataBaseAndCheckIsCorrect(final File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        int i = 0;
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            if (i == 0) {
                if (!setIndexAndCheckIsCorrect(line)) {
                    return false;
                }
                ++i;
                continue;
            }
            if (line.size() < EMAIL_NAME_POSITION || line.size() < 1) {
                continue;
            }
            String id = "";
            String firstName = line.get(FIRST_NAME_POSITION);
            String lastName = line.get(LAST_NAME_POSITION);
            String email = line.get(EMAIL_NAME_POSITION);
            if (ID_POSITION != -1) {
                id = line.get(ID_POSITION);
            }
            if (isNotNullOrEmpty(firstName, lastName, email)) {
                if (isNotNullOrEmpty(id)) {
                    updateContactEmail(id, email);
                } else {
                    updateContactEmail(firstName, lastName, email);
                }
            }
        }
        scanner.close();
        return true;
    }

    private boolean setIndexAndCheckIsCorrect(final List<String> line){
        if (!line.containsAll(Arrays.asList(FIRST_NAME, LAST_NAME, EMAIL))) {
            return false;
        }
        EMAIL_NAME_POSITION = line.indexOf("email");
        FIRST_NAME_POSITION = line.indexOf("first_name");
        LAST_NAME_POSITION = line.indexOf("last_name");
        if (line.contains(ID)) {
            ID_POSITION = line.indexOf("id");
        } else {
            ID_POSITION = -1;
        }
        return true;
    }

    private void updateContactEmail(String firstName, String lastName, String email) {
        LinkedInContact linkedInContact = contactRepository.getFirstByFirstNameAndLastName(firstName, lastName);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

    private void updateContactEmail(String idString, String email) {
        Long id = Long.valueOf(idString.trim());
        LinkedInContact linkedInContact = contactRepository.getById(id);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

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

    /**
     * @author  Ichanskiy
     *
     * This is the method get predictes by input param.
     * @param contactsMessage input object with param.
     * @param root root object predicates.
     * @param builder CriteriaBuilder object.
     */
    private void getPredicatesByParam(ContactsMessage contactsMessage, Root<LinkedInContact> root, CriteriaBuilder builder) {
        predicates.clear();
        if (contactsMessage.getPosition() != null && !contactsMessage.getPosition().isEmpty()) {
            predicates.add(builder.like(root.get("role"), "%" + contactsMessage.getPosition() + "%" ));
        }
        if (contactsMessage.getLocation() != null && !contactsMessage.getLocation().isEmpty()) {
            Location location = locationRepository.getLocationByLocation(contactsMessage.getLocation());
            if (location != null) {
                predicates.add(builder.equal(root.get("location"), location));
            }
        }
        if (contactsMessage.getIndustries() != null && !contactsMessage.getIndustries().isEmpty()) {
            predicates.add(builder.like(root.get("industries"),"%" + contactsMessage.getIndustries() + "%" ));
        }
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get count contacts by predicates.
     * @param predicates input predicates.
     * @return count contacts
     */
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

    private boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private boolean isNotNullOrEmpty(String... s) {
        for (String s1 : s) {
            if (s1 == null || s1.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
