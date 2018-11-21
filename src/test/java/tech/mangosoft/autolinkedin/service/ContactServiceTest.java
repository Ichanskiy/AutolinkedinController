package tech.mangosoft.autolinkedin.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.application.properties")
class ContactServiceTest {

    private static final String INDUSTRIES = "industries";
    private static final String NEW_INDUSTRIES = "newIndustries";
    private static final String LOCATION = "location";
    private static final String POSITION = "position";
    private static final String NEW_POSITION = "newPosition";
    private static final String USER_NAME = "ContactServiceTest";
    private static final Integer PAGE = 1;

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private IAssignmentRepository assignmentRepository;

    private Location location;

    @BeforeEach
    void setUp() {
        location = locationRepository.save(new Location().setLocation(LOCATION));
    }

    @AfterEach
    void tearDown() {
        locationRepository.delete(locationRepository.getLocationByLocation(LOCATION));
    }

    @Test
    @DisplayName("Get contacts by param without bound")
    void getContactsByParamWithoutBoundTest() {
        List<LinkedInContact> contactsDB = contactRepository.saveAll(getLinkedInContacts());
        List<LinkedInContact> contactsFromMethod = contactService.getContactsByParamWithoutBound(getContactsMessage());
        try {
            assertArrayEquals(contactsDB.stream().mapToLong(LinkedInContact::getId).sorted().toArray(),
                    contactsFromMethod.stream().mapToLong(LinkedInContact::getId).sorted().toArray());
        } catch (Exception e) {
            contactRepository.deleteAll(contactsDB);
        }
    }

    @Test
    @Disabled
    void getContactsByStatusAndPageAndSizeTest() {
        Account account = accountRepository.save(getAccount());
        Assignment assignment = assignmentRepository.save(getAssignment()
                .setAccount(account));
        List<LinkedInContact> contactsDB = contactRepository
                .saveAll(getLinkedInContacts()
                        .stream()
                        .map(contact -> contact
                                .setAssignments(new HashSet<>(Collections.singletonList(assignment))))
                        .collect(Collectors.toList()));
        List<LinkedInContact> contactsFromMethod = contactService
                .getContactsByStatusNotAndPageAndSize(account, assignment, Status.STATUS_NEW.getId(), 1, 40);
        try {
            assertArrayEquals(contactsDB.stream().mapToLong(LinkedInContact::getId).sorted().toArray(),
                    contactsFromMethod.stream().mapToLong(LinkedInContact::getId).sorted().toArray());
        } catch (Exception e) {
            contactRepository.deleteAll(contactsDB);
            assignmentRepository.delete(assignment);
            accountRepository.delete(account);
        }
    }

    @Test
    void getCountContactsByStatusTest() {
        Account account = accountRepository.save(getAccount());
        Assignment assignment = assignmentRepository.save(getAssignment()
                .setAccount(account));
        int count = contactService.getCountContactsByStatus(account, assignment, Status.STATUS_NEW.getId());
        try {
            assertEquals(count, 0);
        } catch (Exception e) {
            assignmentRepository.delete(assignment);
            accountRepository.delete(account);
        }
    }

    @Test
    void updateTest() {
        LinkedInContact linkedInContact = new LinkedInContact()
                .setLocation(location)
                .setRole(POSITION)
                .setIndustries(INDUSTRIES);
        contactRepository.save(linkedInContact);
        UpdateContactMessage updateContactMessage = new UpdateContactMessage()
                .setLocation(location.getLocation())
                .setRole(NEW_POSITION)
                .setIndustries(NEW_INDUSTRIES);
        LinkedInContact linkedInContactFromMethod = contactService.update(linkedInContact, updateContactMessage);
        try {
            assertEquals(linkedInContactFromMethod.getIndustries(), updateContactMessage.getIndustries());
            assertEquals(linkedInContactFromMethod.getRole(), updateContactMessage.getRole());
            assertEquals(linkedInContactFromMethod.getLocation().getLocation(), updateContactMessage.getLocation());
        } catch (Exception e) {
            contactRepository.delete(linkedInContact);
        }
    }

    private List<LinkedInContact> getLinkedInContacts() {
        List<LinkedInContact> contacts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            LinkedInContact contact = new LinkedInContact();
            contact.setIndustries(INDUSTRIES);
            contact.setRole(POSITION);
            contact.setLocation(location);
            contacts.add(contact);
        }
        return contacts;
    }

    private ContactsMessage getContactsMessage() {
        ContactsMessage contactsMessage = new ContactsMessage();
        contactsMessage.setPage(PAGE);
        contactsMessage.setPosition(POSITION);
        contactsMessage.setLocation(LOCATION);
        contactsMessage.setIndustries(INDUSTRIES);
        return contactsMessage;
    }

    private Assignment getAssignment() {
        return new Assignment()
                .setStatus(Status.STATUS_NEW);
    }

    private Account getAccount() {
        return new Account()
                .setUsername(USER_NAME);
    }

}