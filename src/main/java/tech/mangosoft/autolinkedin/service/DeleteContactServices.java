package tech.mangosoft.autolinkedin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.IContactProcessingRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class DeleteContactServices {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ILinkedInContactRepository linkedInContactRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IContactProcessingRepository contactProcessingRepository;

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private EntityManagerFactory emf;

    public void testDelete() {
//        ContactsMessage contactsMessage = new ContactsMessage();
//        contactsMessage.setIndustries("Financial Services");
//        contactsMessage.setLocation(locationRepository.getById(1L).getLocation());
//        contactsMessage.setPosition("CTO");
//        contactsMessage.setUserId(2L);

        List<LinkedInContact> contacts = linkedInContactRepository.getAllByAssignments(assignmentRepository.getById(1256L));
        System.out.println(contacts);
        int i = 0;
//        List<LinkedInContact> contacts = contactService.getListContactsByParams(contactsMessage);
        for (LinkedInContact contact : contacts) {
//            List<ContactProcessing> allByAssignmentId = contactProcessingRepository.getAllByAssignmentId(1256L);
//            for (ContactProcessing contactProcessing : allByAssignmentId) {
//                delete(contactProcessing);
//                i++;
////                delete(contact);
//                System.out.println("i === " + i);
//            }
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

            Iterator<Assignment> iterator = assignments.iterator();
            while (iterator.hasNext()) {
                Assignment setElement = iterator.next();
                iterator.remove();
                lc.removeAssignment(setElement);
            }
        }
        entityManager.remove(lc);
        tx.commit();
        entityManager.close();
    }

}
