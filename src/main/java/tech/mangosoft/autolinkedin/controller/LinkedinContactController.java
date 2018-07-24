package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import tech.mangosoft.autolinkedin.ContactService;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;
import tech.mangosoft.autolinkedin.filestorage.FileStorage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contact")
public class LinkedinContactController {

    private Logger logger = Logger.getLogger(LinkedinContactController.class.getName());

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private ILinkedInContactRepository contactRepository;

    private Long linkedInContactId;

    private Long assignmentId;

    @Autowired
    private FileStorage fileStorage;

    @CrossOrigin
    @GetMapping(value = "/getContacts")
    public ResponseEntity<List<LinkedInContact>> getContacts(ContactsMessage contactsMessage) {
        List<LinkedInContact> linkedInContacts = contactService.getContactsByParam(contactsMessage);
        if (linkedInContacts == null) {
            logger.log(Level.WARNING, "Param must be not null or error parsing date");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInContacts, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContact")
    public ResponseEntity<LinkedInContact> getContact(Long id) throws IOException {
        LinkedInContact linkedInContact = contactRepository.getById(id);
        if (linkedInContact == null) {
            logger.log(Level.WARNING, "Param must be not null or error parsing date");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Location location = locationRepository.getLocationByLocation("Vancouver, Canada Area");
        contactService.createCsvFile(location);
        linkedInContactId = linkedInContact.getId();
        return new ResponseEntity<>(linkedInContact, HttpStatus.OK);
    }

    /*
     * Get contact to update
     */
    @CrossOrigin
    @GetMapping(value = "/getContactToUpdate")
    public ResponseEntity<LinkedInContact> updateContact() {
        LinkedInContact linkedInContact = contactRepository.getById(linkedInContactId);
        if (linkedInContact == null) {
            logger.log(Level.WARNING, "linkedInContact must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInContact, HttpStatus.OK);
    }

    @CrossOrigin
    @PutMapping(value = "/update")
    public ResponseEntity<LinkedInContact> updateContact(UpdateContactMessage updateContactMessage) {
        if (updateContactMessage == null) {
            logger.log(Level.WARNING, "UpdateContactMessage must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LinkedInContact linkedInContactDB = contactRepository.getById(updateContactMessage.getId());
        if (linkedInContactDB == null) {
            logger.log(Level.WARNING, "LinkedInContact must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LinkedInContact linkedInContactAfterUpdate = contactService.update(linkedInContactDB, updateContactMessage);
        return new ResponseEntity<>(linkedInContactAfterUpdate, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsProcessed")
    public ResponseEntity<List<LinkedInContact>> getProcessedContact(Long assignmentId, String login) {
        Account account = accountRepository.getAccountByUsername(login);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (assignmentId == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(assignmentId);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getProcessedContact(account, assignment);
        return new ResponseEntity<>(linkedInContacts, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsSucceed")
    public ResponseEntity<List<LinkedInContact>> getContactsSucceed(Long assignmentId, String login) {
        Account account = accountRepository.getAccountByUsername(login);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (assignmentId == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(assignmentId);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsSucceed(account, assignment);
        return new ResponseEntity<>(linkedInContacts, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsFailed")
    public ResponseEntity<List<LinkedInContact>> getContactsFailed(Long assignmentId, String login) {
        Account account = accountRepository.getAccountByUsername(login);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (assignmentId == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(assignmentId);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsFailed(account, assignment);
        return new ResponseEntity<>(linkedInContacts, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsSaved")
    public ResponseEntity<List<LinkedInContact>> getContactsSaved(Long assignmentId, String login) {
        Account account = accountRepository.getAccountByUsername(login);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (assignmentId == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(assignmentId);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsSaved(account, assignment);
        return new ResponseEntity<>(linkedInContacts, HttpStatus.OK);
    }

    /*
     * Download Files
     */
    @CrossOrigin
    @GetMapping("/all")
    public List<String> getListFiles(String location) {
        Location locationDB = locationRepository.getLocationByLocation(location);
        if (locationDB == null) {
            logger.log(Level.WARNING, "Location must be not null");
            return null;
        }
        try {
            contactService.createCsvFile(locationDB);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error create csv");
            return null;
        }
        return fileStorage.loadFiles().map(
                path -> MvcUriComponentsBuilder.fromMethodName(LinkedinContactController.class,
                        "downloadFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource file = fileStorage.loadFile();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}
