package tech.mangosoft.autolinkedin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.IContactProcessingRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Set;

@Service
public class DeleteContactServices {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private EntityManagerFactory emf;

    public void testDelete() {
        ContactsMessage contactsMessage = new ContactsMessage();
        contactsMessage.setIndustries("Financial Services");
        contactsMessage.setLocation(locationRepository.getById(1L).getLocation());
        contactsMessage.setPosition("CTO");
        contactsMessage.setUserId(2L);

        int i = 0;
        List<LinkedInContact> contacts = contactService.getListContactsByParams(contactsMessage);
        for (LinkedInContact contact : contacts) {
            i++;
            delete(contact);
            System.out.println("i = " + i);
        }

        System.out.println(contacts.size());
    }

    private void delete(LinkedInContact contact) {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        LinkedInContact lc = entityManager.find(LinkedInContact.class, contact.getId());
        if (lc.getAssignments() != null) {
            Set<Assignment> assignments = lc.getAssignments();
            for (Assignment assignment : assignments) {
                lc.removeAssignment(assignment);
            }
        }
        entityManager.remove(lc);
        tx.commit();
        entityManager.close();
    }

}
