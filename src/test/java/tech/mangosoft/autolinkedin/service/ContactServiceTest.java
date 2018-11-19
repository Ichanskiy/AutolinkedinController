package tech.mangosoft.autolinkedin.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;
import tech.mangosoft.autolinkedin.db.repository.IContactProcessingRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.application.properties")
class ContactServiceTest {

    private static final String INDUSTRIES = "industries";
    private static final String LOCATION = "location";
    private static final String POSITION = "position";
    private static final Integer PAGE = 1;

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private ContactService contactService;

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
    void getContactsByParamTest() {
    }

    @Test
    void getContactsByStatusTest() {
    }

    @Test
    void getCountContactsByStatusTest() {
    }

    @Test
    void updateTest() {
    }

    private List<LinkedInContact> getLinkedInContacts(){
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

    private ContactsMessage getContactsMessage(){
        ContactsMessage contactsMessage = new ContactsMessage();
        contactsMessage.setPage(PAGE);
        contactsMessage.setPosition(POSITION);
        contactsMessage.setLocation(LOCATION);
        contactsMessage.setIndustries(INDUSTRIES);
        return contactsMessage;
    }

}