package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import tech.mangosoft.autolinkedin.ContactService;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.ProcessedContactMessage;
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

    static final Integer COUNT_TO_PAGE = 40;
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

    @Autowired
    private FileStorage fileStorage;

    @CrossOrigin
    @PostMapping(value = "/getContacts")
    public ResponseEntity<PageImpl<LinkedInContact>> getContacts(@RequestBody ContactsMessage contactsMessage) {
        return new ResponseEntity<>(contactService.getContactsByParam(contactsMessage), HttpStatus.OK);
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
    public ResponseEntity<PageImpl<LinkedInContact>> getProcessedContact(ProcessedContactMessage processedContactMessage) {
        Account account = accountRepository.getAccountByUsername(processedContactMessage.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (processedContactMessage.getAssignmentId() == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(processedContactMessage.getAssignmentId());
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getProcessedContact(account, assignment, processedContactMessage.getPage(), COUNT_TO_PAGE);
        Integer count = contactService.getCountSavedContact(account, assignment);
        return new ResponseEntity<>(new PageImpl<>(linkedInContacts, PageRequest.of(processedContactMessage.getPage() - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsSucceed")
    public ResponseEntity<PageImpl<LinkedInContact>> getContactsSucceed(ProcessedContactMessage processedContactMessage) {
        Account account = accountRepository.getAccountByUsername(processedContactMessage.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (processedContactMessage.getAssignmentId() == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(processedContactMessage.getAssignmentId());
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsSucceed(account, assignment, processedContactMessage.getPage(), COUNT_TO_PAGE);
        Integer count = contactService.getCountSavedContact(account, assignment);
        return new ResponseEntity<>(new PageImpl<>(linkedInContacts, PageRequest.of(processedContactMessage.getPage() - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsFailed")
    public ResponseEntity<PageImpl<LinkedInContact>> getContactsFailed(ProcessedContactMessage processedContactMessage) {
        Account account = accountRepository.getAccountByUsername(processedContactMessage.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (processedContactMessage.getAssignmentId() == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(processedContactMessage.getAssignmentId());
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsFailed(account, assignment, processedContactMessage.getPage(), COUNT_TO_PAGE);
        Integer count = contactService.getCountFailedContact(account, assignment);
        return new ResponseEntity<>(new PageImpl<>(linkedInContacts, PageRequest.of(processedContactMessage.getPage() - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getContactsSaved")
    public ResponseEntity<PageImpl<LinkedInContact>> getContactsSaved(ProcessedContactMessage processedContactMessage) {
        Account account = accountRepository.getAccountByUsername(processedContactMessage.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (processedContactMessage.getAssignmentId() == null) {
            logger.log(Level.WARNING, "Id must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = assignmentRepository.getById(processedContactMessage.getAssignmentId());
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<LinkedInContact> linkedInContacts = contactService.getContactsSaved(account, assignment, processedContactMessage.getPage(), COUNT_TO_PAGE);
        Integer count = contactService.getCountSavedContact(account, assignment);
        return new ResponseEntity<>(new PageImpl<>(linkedInContacts, PageRequest.of(processedContactMessage.getPage() - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
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
