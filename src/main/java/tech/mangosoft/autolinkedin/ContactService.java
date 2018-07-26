package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Value("${storage.path}")
    private String path;

    @Value("${storage.filename}")
    private String filename;


    /**
     * @author  Ichanskiy
     *
     * This is the method get list contact.
     * @param p input object with param.
     * @return list Contacts
     */
    public List<LinkedInContact> getContactsByParam(ContactsMessage p){
        if (p == null) {
            return null;
        }
        Location location = locationRepository.getLocationByLocation(p.getLocation());
        if (dateLengthEqualsNotZero(p)) {
            return contactRepository.findAllByLocationAndRoleContains(location, p.getPosition(), PageRequest.of(p.getPage() - 1, 40,  Sort.Direction.DESC, "id"));
        }
        Date firsDate = getDate(p.getFirstDate());
        Date secondDate = getDate(p.getSecondDate());
        if (secondDate == null || firsDate == null) {
            return null;
        }
        return contactRepository.findAllByLocationAndRoleContainsAndCreateTimeBetween(location, p.getPosition(), firsDate, secondDate, PageRequest.of(p.getPage() - 1, 40,  Sort.Direction.DESC, "id"));
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get count contact.
     * @param p input object with param.
     * @return count Contacts
     */
    public Long getCountByParam(ContactsMessage p) {
        if (p == null) {
            return null;
        }
        Location location = locationRepository.getLocationByLocation(p.getLocation());
        if (dateLengthEqualsNotZero(p)) {
            return contactRepository.countAllByLocationAndRoleContains(location, p.getPosition());
        }
        Date firsDate = getDate(p.getFirstDate());
        Date secondDate = getDate(p.getSecondDate());
        if (secondDate == null || firsDate == null) {
            return null;
        }
        return contactRepository.countAllByLocationAndRoleContainsAndCreateTimeBetween(location, p.getPosition(), firsDate, secondDate);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method get bollean length result
     * @param p input object with param.
     * @return true if length more then 0
     */
    private boolean dateLengthEqualsNotZero(ContactsMessage p){
        return p.getFirstDate().length() == 0 || p.getSecondDate().length() == 0;
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
                .map(ContactProcessing::getContact)
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
                .map(ContactProcessing::getContact)
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
                .map(ContactProcessing::getContact)
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
