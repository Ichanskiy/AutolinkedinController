package tech.mangosoft.autolinkedin.db.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.db.entity.*;

import javax.transaction.Transactional;
import java.util.List;

import static tech.mangosoft.autolinkedin.db.entity.LinkedInContact.STATUS_ACQUIRED;

@Service
public class LinkedInContactRepositoryCustomImpl implements ILinkedInContactRepositoryCustom {

    private static Logger log = LogManager.getRootLogger();

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private IProcessingReportRepository processingReportRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Transactional
    @Override
    public LinkedInContact getNextAvailableContact(int page, Assignment assignment) {
//        Page<LinkedInContact> linkedInContacts = contactRepository.findAllByStatus(LinkedInContact.STATUS_NEW, PageRequest.of(page, 1));
        LinkedInContact contact = null;
        if (assignment.getPosition() != null && assignment.getIndustries() != null && assignment.getFullLocationString() != null) {
            Location location = locationRepository.getLocationByLocation(assignment.getFullLocationString());
            if (location != null) {
                contact = contactRepository.findFirstByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, location, assignment.getPosition(), assignment.getIndustries());
            }
        }
        if (contact == null) {
            contact = contactRepository.findFirstByStatusAndRoleContainsAndContactProcessingsIsNull(LinkedInContact.STATUS_NEW, assignment.getPosition());
        }
        if (contact == null) {
            log.error("Can't retrieve new contact from db");
            return null;
        }
        return contactRepository.save(contact.setStatus(STATUS_ACQUIRED));
    }


    @Transactional
    @Override
    public boolean updateContactStatus(LinkedInContact contact, Account account, int status, String error, String audit, Long processingReportId) {

        ProcessingReport report = processingReportRepository.getById(processingReportId);

        ContactProcessing contactProcessing = contactProcessingRepository.findByAccountIdAndContactId(account.getId(), contact.getId());
        contactRepository.findById(contact.getId());
        if (contactProcessing == null) {
            contactProcessing = new ContactProcessing();
            contactProcessing.setAccount(account);
            contactProcessing.setContact(contact);
        }
        contactProcessing.setStatus(status);
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
    public boolean saveNewContactsBatch(List<LinkedInContact> contacts, Long processingReportId) {

        ProcessingReport report = processingReportRepository.getById(processingReportId);

        if (contacts != null){
            for (LinkedInContact contact:contacts) {
                if (contactRepository.existsLinkedInContactByFirstNameAndLastNameAndCompanyName(contact.getFirstName(), contact.getLastName(), contact.getCompanyName())) {
                    log.error("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " already exists");
                } else {
                    log.info("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " saved");
                    try {
                        contactRepository.save(contact);
                    } catch (Exception e) {
                        log.info("Contact " + contact.getFirstName() + " " + contact.getLastName() +" "+ contact.getCompanyName() + " not saved");
                        report.incrementFailed(1L);
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
}
