package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;
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
        if (p.getFirstDate().length() == 0 || p.getSecondDate().length() == 0) {
            return contactRepository.findAllByLocationAndRoleContains(location, p.getPosition(), PageRequest.of(p.getPage() - 1, 40,  Sort.Direction.DESC, "id"));
        }
        Date firsDate = getDate(p.getFirstDate());
        Date secondDate = getDate(p.getSecondDate());
        if (secondDate == null || firsDate == null) {
            return null;
        }
        return contactRepository.findAllByLocationAndRoleContainsAndCreateTimeBetween(location, p.getPosition(), firsDate, secondDate, PageRequest.of(p.getPage() - 1, 40,  Sort.Direction.DESC, "id"));
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
            System.out.println(date);
            System.out.println(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
