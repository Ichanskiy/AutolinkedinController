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


    public void createCsvFileByParam(ContactsMessage message) throws IOException {
        List<LinkedInContact> contacts = getContactsByParamWithoutBound(message);
        writeToExcel(contacts);
    }

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

    private void writeToExcel(final List<LinkedInContact> contacts) throws IOException {
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
            row.createCell(9).setCellValue(getUserFullNameWhichAddCurrentContact(contact));
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
    }

    private String getUserFullNameWhichAddCurrentContact(LinkedInContact contact) {
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

    public boolean readFromExcel(final File file) throws IOException {
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

                    } else {
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
            return false;
        }
    }

    private void updateContactEmail(String firstName, String lastName, String email) {
        LinkedInContact linkedInContact = contactRepository.getFirstByFirstNameAndLastName(firstName, lastName);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

    private void updateContactEmail(Long id, String email) {
        LinkedInContact linkedInContact = contactRepository.getById(id);
        if (linkedInContact != null) {
            linkedInContact.setEmail(email);
            contactRepository.save(linkedInContact);
        }
    }

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
     * This is the method get predictes by input param.
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

    private boolean accountIsNullOrIsAdmin(ContactsMessage contactsMessage) {
        if (contactsMessage.getUserId() == null) {
            return true;
        }
        Account account = accountRepository.getById(contactsMessage.getUserId());
        if (account == null) {
            return true;
        }
        return account.isAdmin();
    }

    /**
     * @param message input params.
     * @return count contacts
     * @author Ichanskiy
     * <p>
     * This is the method get count contacts by predicates.
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

    public Integer getCountContactsByStatus(Account account, Assignment assignment, Integer status) {
        return contactProcessingRepository
                .countDistinctByAccountAndAssignmentAndStatus(account, assignment, status);
    }


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
