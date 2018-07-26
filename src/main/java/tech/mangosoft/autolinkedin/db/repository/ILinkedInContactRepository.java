package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public interface ILinkedInContactRepository extends JpaRepository<LinkedInContact, Long> {
/*
    @Query("SELECT c FROM LinkedInContact c WHERE c.status= 0 AND (LOWER(c.assignedLinkedinContact) = LOWER(:assignedContact) OR c.assignedLinkedinContact IS NULL ) ORDER BY c.id DESC")
    public List<LinkedInContact> findLinkedInContactsFor(@Param("assignedContact") String assignedContact, Pageable pageable );

    final String markContactQuery = "update linkedin_contacts c set c.assigned_linkedin_contact =:assignedContact, c.assigned_time = NOW(), c.status = "+ LinkedInContact.STATUS_ACQUIRED +
            " WHERE c.status= "+LinkedInContact.STATUS_NEW +" AND (LOWER(c.assigned_linkedin_contact) = LOWER(:assignedContact) OR c.assigned_linkedin_contact IS NULL ) LIMIT 1";
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = markContactQuery, nativeQuery = true)
    void markContact(@Param("assignedContact") String assignedContact);

    final String findAssignedLinkedInContactsForQuery = "SELECT c FROM LinkedInContact c " +
            "WHERE c.status= "+ LinkedInContact.STATUS_ACQUIRED +" AND LOWER(c.assignedLinkedinContact) = LOWER(:assignedContact) ORDER BY c.id DESC";
    @Query(findAssignedLinkedInContactsForQuery)
    public List<LinkedInContact> findAssignedLinkedInContactsFor(@Param("assignedContact") String assignedContact, Pageable pageable );
*/

//    LinkedInContact findFirstByStatusAndLocationAndRoleLikeAndIndustriesLikeAndContactProcessingsIsNull(int status, Location location, String role, String industries);
//    Page<LinkedInContact> findAllByStatus(int status, Pageable pageable);
//    LinkedInContact findFirstByStatusAndRoleLikeAndIndustriesContainsAndLocation(int status, String role, String industries, Location location);
//    LinkedInContact findFirstByStatusAndContactProcessingsIsNull(int status);

    LinkedInContact findFirstByStatusAndRoleContainsAndContactProcessingsIsNull(int status, String role);

    LinkedInContact findFirstByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(int status, Location location, String role, String industries);

    List<LinkedInContact> findAllByLocationAndRoleContainsAndCreateTimeBetween(Location location, String role, Date after, Date before, Pageable pageable);

    Integer countAllByLocationAndRoleContainsAndCreateTimeBetween(Location location, String role, Date after, Date before, Pageable pageable);

    List<LinkedInContact> findAllByLocationAndRoleContains(Location location, String industries, Pageable pageable);

    Integer countAllByLocationAndRoleContains(Location location, String industries);

    List<LinkedInContact> findAllByLocation(Location location, Pageable pageable);

    LinkedInContact getById(Long id);

    @Query("select c.id " +
        "from LinkedInContact c " +
        "join ContactProcessing processing on c.id = processing.contact.id " +
        "join Account a on processing.account.id = a.id " +
        "where a.username = :username " +
        "order by c.id")
    Page<Long> getAvailableContact(@Param("username") String username, Pageable pageable);

//    @Query("select c.id " +
//        "from LinkedInContact c " +
//        "where 1=1 and  c.role = :username " +
//        "order by c.id")
//    Page<Long> getAvailableContact(@Param("username") String username, Pageable pageable);

    public boolean existsLinkedInContactByFirstNameAndLastNameAndCompanyName(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("companyName") String companyName);

    @Query("select c.companyName, c.firstName, c.lastName, c.role, c.linkedin, l.location, c.email from LinkedInContact c join Location l on c.location = l.id  where  l.id = :locationId")
    List getContactsToCsv(@Param("locationId") Long locationId);
}
