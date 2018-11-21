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
import tech.mangosoft.autolinkedin.utils.CSVUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static tech.mangosoft.autolinkedin.utils.CSVUtils.parseLine;

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
    private static final String[] HEADERS = {"id", "company_name", "company_website", "first_name", "last_name",
            "role", "person_linkedin", "location", "industries", "user", "email", "headcount"};
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
        writeToCSVFile(contacts);
//        writeToExcel(contacts);
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

    private void writeToCSVFile(final List<LinkedInContact> contactsFromDb) throws IOException {
        String csvFile = path.concat(filename);
        File file = new File(csvFile);
        FileWriter writer = new FileWriter(file.getAbsoluteFile());
        CSVUtils.writeLine(writer, Arrays.asList("id", "company_name", "company_website", "first_name", "last_name", "role", "person_linkedin", "location", "industries", "user", "email"));
        for (LinkedInContact contact : contactsFromDb) {
            if (!isNotNullOrEmpty(contact.getFirstName(), contact.getLastName())) {
                continue;
            }
            CSVUtils.writeLine(writer, Arrays
                    .asList(contact.getId() != null ? contact.getId().toString().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getCompanyName()) ? contact.getCompanyName().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getCompanyWebsite()) ? contact.getCompanyWebsite().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getFirstName()) ? contact.getFirstName().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getLastName()) ? contact.getLastName().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getRole()) ? contact.getRole().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getLinkedin()) ? contact.getLinkedin().replace("|", "/").concat(" ") : " ",
                            contact.getLocation() != null ? contact.getLocation().getLocation().replace("|", "/").concat(" ") : " ",
                            isNotNullOrEmpty(contact.getIndustries()) ? contact.getIndustries().replace("|", "/").concat(" ") : " ",
                            getUserFullNameWhichAddCurrentContact(contact),
                            isNotNullOrEmpty(contact.getEmail()) ? contact.getEmail().replace("|", "/").concat(" ") : " "));
        }
        writer.flush();
        writer.close();
    }

    private void writeToExcel(final List<LinkedInContact> contacts) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Contacts");
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        Row headerRow = sheet.createRow(0);
        for(int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
        }
        int rowNum = 1;
        for(LinkedInContact contact : contacts){
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

        for(int i = 0; i < HEADERS.length; i++) {
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

    private boolean setIndexAndCheckIsCorrect(final List<String> line) {
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
                PageRequest.of(message.getPage(), COUNT_FOR_PAGE), getCountContactsByPredicates(predicates));
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
            predicates.add(builder.equal(root.join("assignments").get("account").get("id"),
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
     * @param predicates input predicates.
     * @return count contacts
     * @author Ichanskiy
     * <p>
     * This is the method get count contacts by predicates.
     */
    private Long getCountContactsByPredicates(List<Predicate> predicates) {
        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(LinkedInContact.class)));
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
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
