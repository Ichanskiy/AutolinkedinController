package tech.mangosoft.autolinkedin.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IContactProcessingRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <h1> LinkedIn Service!</h1>
 * The LinkedInService implements initial logic application
 * <p>
 * <p>
 * Method annotate @Scheduled is point to start do assignment
 * user friendly and it is assumed as a high quality code.
 *
 * @author Ichanskiy
 * @version 1.0
 * @since 2018-06-06
 */
@Service
public class ContactService {

    private static Logger logger = Logger.getLogger(ContactService.class.getName());

    private static final Integer COUNT_FOR_PAGE = 40;
    private static final List<String> HEADERS = Arrays.asList("id", "company_name", "company_website", "first_name", "last_name",
            "role", "person_linkedin", "location", "industries", "user", "email", "headcount");
    private Integer FIRST_NAME_POSITION = -1;
    private Integer LAST_NAME_POSITION = -1;
    private Integer EMAIL_POSITION = -1;
    private Integer ID_POSITION = -1;
    private List<Predicate> predicates = new ArrayList<>();

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${storage.path}")
    private String path;

    @Value("${storage.filename}")
    private String filename;


    /**
     * @param message input object with param.
     * @author Ichanskiy
     * This method get all contacts by param ant write this lisct to exel file
     * <p>
     */
    public void createExcelFileByParam(ContactsMessage message) throws IOException {
        logger.info("Starting create excel file by param...");
        List<LinkedInContact> contacts = getContactsByParamWithoutBound(message);
        logger.info("Contacts size by param = " + contacts.size());
        writeToExcel(contacts);
    }


    /**
     * @param message input object with param.
     * @return all contacts by param from message without limit
     * @author Ichanskiy
     * <p>
     */
    List<LinkedInContact> getContactsByParamWithoutBound(ContactsMessage message) {
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

    /**
     * @author VestDev
     * This method write to exel contacts info (id, contact's companyName, contact's company website,
     * contact's first name, contact's last name, contact's position, contact's linkedin,
     * contact's location, contact's industries, account full name who add this contact to database,
     * contact's email, contact's headcount
     * <p>
     */
    private void writeToExcel(final List<LinkedInContact> contacts) throws IOException {
        logger.info("Starting write to excel...");

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Contacts");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS.get(i));
        }
        int rowNum = 1;
        for (LinkedInContact contact : contacts) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(contact.getId());
            row.createCell(1).setCellValue(contact.getCompanyName());
            row.createCell(2).setCellValue(contact.getCompanyWebsite());
            row.createCell(3).setCellValue(contact.getFirstName());
            row.createCell(4).setCellValue(contact.getLastName());
            row.createCell(5).setCellValue(contact.getRole());
            row.createCell(6).setCellValue(contact.getLinkedin());
            row.createCell(7).setCellValue(contact.getLocation() != null ? contact.getLocation().getLocation() : null);
            row.createCell(8).setCellValue(contact.getIndustries());
            row.createCell(9).setCellValue(getAccountFullNameWhichAddCurrentContact(contact));
            row.createCell(10).setCellValue(contact.getEmail());
            row.createCell(11).setCellValue(contact.getHeadcount() != null ? contact.getHeadcount().getHeadcount() : null);
        }

        for (int i = 0; i < HEADERS.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fileOut = new FileOutputStream(path + filename);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        logger.info("Write to excel is success");
    }

    /**
     * @param contact input
     * @author VestDev
     * @return account full name who add this contact to database
     * <p>
     */
    private String getAccountFullNameWhichAddCurrentContact(LinkedInContact contact) {
        Set<Assignment> assignments = contact.getAssignments();
        if (!CollectionUtils.isEmpty(assignments)) {
            Optional<Assignment> assignment = assignments.stream()
                    .filter(a -> !a.getTask().equals(Task.TASK_CONNECTION))
                    .min(Comparator.comparing(Assignment::getUpdateTime));
            if (assignment.isPresent()) {
                return assignment.get().getAccount() != null ? assignment.get().getAccount().getCaption() : null;
            }
        }
        return null;
    }

    /**
     * @param file input file
     * @author VestDev
     * This methid read and set position index to variable
     * @return if successfully - true, else - false
     * <p>
     */
    public boolean readFromExcel(final File file) throws IOException {
        logger.info("Read From Excel started");
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet.getPhysicalNumberOfRows() > 0) {
            for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (i == 0) {
                    if (row.getPhysicalNumberOfCells() > 0) {
                        for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                            if (ID_POSITION == -1) {
                                if (row.getCell(j).getStringCellValue().equalsIgnoreCase(HEADERS.get(0))) {
                                    ID_POSITION = j;
                                }
                            }
                            if (FIRST_NAME_POSITION == -1) {
                                if (row.getCell(j).getStringCellValue().equalsIgnoreCase(HEADERS.get(3))) {
                                    FIRST_NAME_POSITION = j;
                                }
                            }
                            if (LAST_NAME_POSITION == -1) {
                                if (row.getCell(j).getStringCellValue().equalsIgnoreCase(HEADERS.get(4))) {
                                    LAST_NAME_POSITION = j;
                                }
                            }
                            if (EMAIL_POSITION == -1) {
                                if (row.getCell(j).getStringCellValue().equalsIgnoreCase(HEADERS.get(10))) {
                                    EMAIL_POSITION = j;
                                }
                            }
                        }
                        logger.info("Read position : id = " + ID_POSITION + "firstName = " + FIRST_NAME_POSITION +
                                "lastName = " + LAST_NAME_POSITION + "email = " + EMAIL_POSITION);
                    } else {
                        logger.info("Error read from excel");
                        return false;
                    }
                }
                if (i > 0) {
                    if (row.getCell(ID_POSITION) != null && row.getCell(EMAIL_POSITION) != null) {
                        updateContactEmail((long) row.getCell(ID_POSITION).getNumericCellValue(),
                                row.getCell(EMAIL_POSITION).getStringCellValue());
                    } else if (row.getCell(FIRST_NAME_POSITION) != null && row.getCell(LAST_NAME_POSITION) != null
                            && row.getCell(EMAIL_POSITION) != null) {
                        updateContactEmail(row.getCell(FIRST_NAME_POSITION).getStringCellValue(),
                                row.getCell(LAST_NAME_POSITION).getStringCellValue(),
                                row.getCell(EMAIL_POSITION).getStringCellValue());
                    }
                }
            }
            return true;
        } else {
            logger.info("Error read from excel file");
            return false;
        }
    }

    /**
     * @param firstName contact.
     * @param lastName contact.
     * @param email contact.
     * @author Ichanskiy
     * This  method update first contact`s email by firstName and lastName.
     * <p>
     */
    private void updateContactEmail(String firstName, String lastName, String email) {
        LinkedInContact linkedInContact = contactRepository.getFirstByFirstNameAndLastName(firstName, lastName);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

    /**
     * @param id contact.
     * @param email contact.
     * @author Ichanskiy
     * This  method update contact`s email by id.
     * <p>
     */
    private void updateContactEmail(Long id, String email) {
        LinkedInContact linkedInContact = contactRepository.getById(id);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

    /**
     * @param message input object with param.
     * @return page with contacts by param from message
     * @author Ichanskiy
     * <p>
     */
    public PageImpl<LinkedInContact> getPageContactsByParam(ContactsMessage message) {
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
        return new PageImpl<>(query.getResultList(),
                PageRequest.of(message.getPage(), COUNT_FOR_PAGE), getCountContactsByParams(message));
    }


    /**
     * @param message input object with param.
     * @return contacts by param from message
     * @author Ichanskiy
     * <p>
     */
    public List<LinkedInContact> getListContactsByParams(ContactsMessage message) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LinkedInContact> criteriaQuery = builder.createQuery(LinkedInContact.class);
        Root<LinkedInContact> root = criteriaQuery.from(LinkedInContact.class);
        getPredicatesByParam(message, root, builder);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<LinkedInContact> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    /**
     * @param contactsMessage input object with param.
     * @param root            root object predicates.
     * @param builder         CriteriaBuilder object.
     * @author Ichanskiy
     * <p>
     * This method get predictes by input param.
     */
    private void getPredicatesByParam(ContactsMessage contactsMessage,
                                      Root<LinkedInContact> root,
                                      CriteriaBuilder builder) {
        predicates.clear();

        if (!accountIsNullOrIsAdmin(contactsMessage)) {
            predicates.add(builder.equal(root.joinSet("assignments").join("account").get("id"),
                    contactsMessage.getUserId()));
        }
        if (contactsMessage.getPosition() != null && !contactsMessage.getPosition().isEmpty()) {
            predicates.add(builder.like(root.get("role"), "%" + contactsMessage.getPosition() + "%"));
        }
        if (contactsMessage.getLocation() != null && !contactsMessage.getLocation().isEmpty()) {
            Location location = locationRepository.getLocationByLocation(contactsMessage.getLocation());
            if (location != null) {
                predicates.add(builder.equal(root.get("location"), location));
            }
        }
        if (contactsMessage.getIndustries() != null && !contactsMessage.getIndustries().isEmpty()) {
            predicates.add(builder.like(root.get("industries"), "%" + contactsMessage.getIndustries() + "%"));
        }
    }


    /**
     * @param contactsMessage input params.
     * @return boolean true if userId or account equals null, or account is admin, else - return false
     * @author Ichanskiy
     * <p>
     * This method return.
     */
    private boolean accountIsNullOrIsAdmin(ContactsMessage contactsMessage) {
        if (contactsMessage.getUserId() == null) {
            logger.info("User id is null");
            return true;
        }
        Account account = accountRepository.getById(contactsMessage.getUserId());
        if (account == null) {
            logger.info("Account is null");
            return true;
        }
        logger.info("Account is admin = " + account.isAdmin());
        return account.isAdmin();
    }

    /**
     * @param message input params.
     * @return count contacts
     * @author Ichanskiy
     * <p>
     * This method get count contacts by predicates.
     */
    private Long getCountContactsByParams(ContactsMessage message) {
        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        Root root = cq.from(LinkedInContact.class);
        cq.select(qb.countDistinct(root));

        predicates.clear();
        getPredicatesByParam(message, root, qb);

        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        return entityManager.createQuery(cq).getSingleResult();

    }

    /**
     * @param account input account
     * @param assignment input assignment
     * @param status input status
     * @param page returned page
     * @param size count for page
     * @return list contacts by params
     * @author Ichanskiy
     * <p>
     * This method get sorted contacts (by 'id')
     * by status not equals input status, account, assignment and number page with size contacts per page
     * */
    public List<LinkedInContact> getContactsByStatusNotAndPageAndSize(Account account,
                                                                      Assignment assignment,
                                                                      Integer status,
                                                                      int page, int size) {
        return contactProcessingRepository
                .getDistinctByAccountAndAssignmentAndStatusNot(account, assignment,
                        status, PageRequest.of(page - 1, size, Sort.Direction.DESC, "id"))
                .stream()
                .map(contactProcessing -> contactProcessing.getContact().setComments(contactProcessing.getAuditLog()))
                .collect(Collectors.toList());
    }

    /**
     * @param account input account
     * @param assignment input assignment
     * @param status input status
     * @return count contacts by params
     * @author Ichanskiy
     * <p>
     * This method get count contacts by input account, assignment and status
     * */
    public Integer getCountContactsByStatus(Account account, Assignment assignment, Integer status) {
        return contactProcessingRepository
                .countDistinctByAccountAndAssignmentAndStatus(account, assignment, status);
    }


    /**
     * @param linkedInContactDB input contact
     * @param updateContactMessage input object with new params
     * @return updated contact
     * @author Ichanskiy
     * <p>
     * This method set to contact new value from input object
     */
    public LinkedInContact update(LinkedInContact linkedInContactDB, UpdateContactMessage updateContactMessage) {
        Location location = locationRepository.getLocationByLocationLike(updateContactMessage.getLocation());
        if (location == null) {
            locationRepository.save(new Location(updateContactMessage.getLocation()));
        }
        return contactRepository.save(linkedInContactDB.
                setEmail(updateContactMessage.getEmail())
                .setCompanyName(updateContactMessage.getCompanyName())
                .setCompanyWebsite(updateContactMessage.getCompanySite())
                .setLastName(updateContactMessage.getLastName())
                .setFirstName(updateContactMessage.getFirstName())
                .setRole(updateContactMessage.getRole())
                .setIndustries(updateContactMessage.getIndustries())
                .setLocation(location)
                .setComments(updateContactMessage.getComment()));
    }
}
