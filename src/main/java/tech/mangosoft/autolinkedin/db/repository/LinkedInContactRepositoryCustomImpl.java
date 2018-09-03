package tech.mangosoft.autolinkedin.db.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.db.entity.*;

import javax.transaction.Transactional;
import java.util.*;

import static tech.mangosoft.autolinkedin.db.entity.LinkedInContact.STATUS_ACQUIRED;

@Service
public class LinkedInContactRepositoryCustomImpl implements ILinkedInContactRepositoryCustom {

    private static Logger logger = LogManager.getRootLogger();
    private static String logeMessage = "";

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private IProcessingReportRepository processingReportRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Transactional
    @Override
    public LinkedInContact getNextAvailableContact(Assignment assignment) {
//        Page<LinkedInContact> linkedInContacts = contactRepository.findAllByStatus(LinkedInContact.STATUS_NEW, PageRequest.of(page, 1));
        LinkedInContact contact = null;
        if (assignment.getPosition() != null && assignment.getIndustries() != null && assignment.getFullLocationString() != null) {
            Location location = locationRepository.getLocationByLocationLike(assignment.getFullLocationString());
            if (location != null) {
                contact = contactRepository.findFirstByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition(), assignment.getIndustries());
                if (contact == null) {
                    contact = contactRepository.findFirstByStatusAndLocationAndIndustriesAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getIndustries());
                }
//                if (contact == null) {
//                    contact = contactRepository.findFirstByStatusAndLocationAndRoleContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
//                }
                if (contact == null) {
                    contact = contactRepository.findFirstByStatusAndLocationAndIndustriesContainsAndRoleContains(LinkedInContact.STATUS_NEW, location, assignment.getIndustries(), assignment.getPosition());
                }
                if (contact == null) {
                    contact = contactRepository.findFirstByStatusAndLocationAndIndustriesContains(LinkedInContact.STATUS_NEW, location, assignment.getIndustries());
                }
                if (contact == null) {
                    contact = contactRepository.findFirstByStatusAndLocationAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location);
                }
                if (contact == null) {
                    contact = contactRepository.findFirstByStatusAndLocation(LinkedInContact.STATUS_NEW, location);
                }
                //todo fix
                if (contact == null) {
                    contact = contactRepository.findFirstByLocation(location);
                }
            }
            else {
                logger.error("LOCATION IS NULL");
                return null;
            }
        }
//        if (contact == null) {
//            contact = contactRepository.findFirstByStatusAndRoleContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, assignment.getPosition());
//        }
        if (contact == null) {
            logger.error("Can't retrieve new contact from db");
            return null;
        }
        return contactRepository.save(contact.setStatus(STATUS_ACQUIRED));
    }

    @Transactional
    public List<LinkedInContact> getAllContactsForAssignment(Assignment assignment) {
        List<LinkedInContact> resultContacts = new ArrayList<>();

        Location location = locationRepository.getLocationByLocationLike(assignment.getFullLocationString());
        if (location != null) {
            if (assignment.getPosition() != null && assignment.getIndustries() != null) {
                //query for old users
                List<LinkedInContact> contacts1 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition(), assignment.getIndustries());
                List<LinkedInContact> contacts2 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNullAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                List<LinkedInContact> contacts3 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesContains(LinkedInContact.STATUS_NEW, location, assignment.getPosition(), assignment.getIndustries());
                List<LinkedInContact> contacts4 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                resultContacts.addAll(contacts1);
                resultContacts.addAll(contacts2);
                resultContacts.addAll(contacts3);
                resultContacts.addAll(contacts4);
            }
            if (assignment.getPosition() == null && assignment.getIndustries() != null) {
                List<LinkedInContact> contacts1 = contactRepository.findAllByStatusAndLocationAndIndustriesContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getIndustries());
                List<LinkedInContact> contacts2 = contactRepository.findAllByStatusAndLocationAndIndustriesIsNullAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location);
                List<LinkedInContact> contacts3 = contactRepository.findAllByStatusAndLocationAndIndustriesContains(LinkedInContact.STATUS_NEW, location, assignment.getIndustries());
                List<LinkedInContact> contacts4 = contactRepository.findAllByStatusAndLocationAndIndustriesIsNull(LinkedInContact.STATUS_NEW, location);
                resultContacts.addAll(contacts1);
                resultContacts.addAll(contacts2);
                resultContacts.addAll(contacts3);
                resultContacts.addAll(contacts4);
            }
            if (assignment.getPosition() != null && assignment.getIndustries() == null) {
                List<LinkedInContact> contacts1 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                List<LinkedInContact> contacts2 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNullAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                List<LinkedInContact> contacts3 = contactRepository.findAllByStatusAndLocationAndRoleContains(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                List<LinkedInContact> contacts4 = contactRepository.findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition());
                resultContacts.addAll(contacts1);
                resultContacts.addAll(contacts2);
                resultContacts.addAll(contacts3);
                resultContacts.addAll(contacts4);
            }
            if (assignment.getPosition() == null && assignment.getIndustries() == null) {
                List<LinkedInContact> contacts1 = contactRepository.findAllByStatusAndLocation(LinkedInContact.STATUS_NEW, location);
                resultContacts.addAll(contacts1);
            }
        } else {
            logger.error("LOCATION IS NULL");
            return null;
        }
        return resultContacts;
    }

//    private Collection<? extends LinkedInContact> deleteContactWithCurrentAccount(Account account, List<LinkedInContact> linkedInContacts) {
//        for (LinkedInContact linkedInContact : linkedInContacts) {
//            for (int i = 0; i < linkedInContact.getContactProcessings().size(); i++) {
//                if (linkedInContact.getContactProcessings().get(i).getContact().getId().equals(account.getId())) {
//
//                }
//            }
//        }
//    }


    @Transactional
    @Override
    public boolean updateContactStatus(Assignment assignment, LinkedInContact contact, Account account, int status, String error, String audit, Long processingReportId) {

        ProcessingReport report = processingReportRepository.getById(processingReportId);

        ContactProcessing contactProcessing = contactProcessingRepository.findFirstByAccountIdAndContactId(account.getId(), contact.getId());
        contactRepository.findById(contact.getId());
        if (contactProcessing == null) {
            contactProcessing = new ContactProcessing();
            contactProcessing.setAccount(account);
            contactProcessing.setContact(contact);
        }
        contactProcessing.setStatus(status);
        contactProcessing.setAssignment(assignment);
        contactProcessing.setError(error);
        contactProcessing.setAuditLog(audit);
        ContactProcessing contactProcessingDB = contactProcessingRepository.save(contactProcessing);
        if (contactProcessingDB == null) {
            report.incrementFailed(1L);
            processingReportRepository.save(report);
            return false;
        }
        report.incrementSuccessed(1L);
        report.incrementProcessed(1L);
        report.incrementSaved(1L);
        processingReportRepository.save(report);
        return true;
    }

    @Override
    public boolean saveNewContactsBatch(Account account, Long assignmentId, List<LinkedInContact> contacts, Long processingReportId, String log) {
        logeMessage = log;
        ProcessingReport report = processingReportRepository.getById(processingReportId);
        Assignment assignmentDb = assignmentRepository.getById(assignmentId);

        if (contacts != null && assignmentDb != null){
            for (LinkedInContact contact : contacts) {
                if (contactRepository.existsLinkedInContactByFirstNameAndLastNameAndCompanyName(contact.getFirstName(), contact.getLastName(), contact.getCompanyName())) {
                    LinkedInContact linkedInContactDB = contactRepository.getFirstByFirstNameAndLastName(contact.getFirstName(), contact.getLastName());
                    if (linkedInContactDB != null) {
//                        linkedInContactDB.setAssignment(assignmentDb);
                        contactRepository.save(linkedInContactDB);
                        contactProcessingRepository.save(getNewContactProcessing(account, ContactProcessing.STATUS_GRABBED, linkedInContactDB, assignmentDb));
                    }
                    logger.error("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " already exists");
                } else {
                    logger.info("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " saved");
                    try {
//                        contact.setAssignment(assignmentDb);
                        LinkedInContact linkedInContactDB = contactRepository.save(contact);
                        contactProcessingRepository.save(getNewContactProcessing(account, ContactProcessing.STATUS_GRABBED, linkedInContactDB, assignmentDb));
                    } catch (Exception e) {
                        logger.info("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " not saved");
                        report.incrementFailed(1L);
                        contactProcessingRepository.save(getNewContactProcessing(account, ContactProcessing.STATUS_ERROR, contactRepository.save(contact), assignmentDb));
                    }
                    report.incrementSaved(1L);
                }
            }
            report.incrementSuccessed((long) contacts.size());
            report.incrementProcessed((long) contacts.size());
        }
        processingReportRepository.save(report);
        return true;
    }

    private ContactProcessing getNewContactProcessing(Account account, int status, LinkedInContact contact, Assignment assignment){
        return new ContactProcessing()
                .setContact(contact)
                .setAccount(account)
                .setStatus(status)
                .setAssignment(assignment)
                .setAuditLog(logeMessage);
    }
}
