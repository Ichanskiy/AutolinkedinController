package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.entity.Location;

import java.util.List;


public interface ILinkedInContactRepository extends JpaRepository<LinkedInContact, Long> {
    
    LinkedInContact findFirstByStatusAndLocationAndContactProcessingsIsNull(int status, Location location);

    LinkedInContact findFirstByLocation(Location location);

    LinkedInContact findFirstByStatusAndLocation(Integer status, Location location);
    
    List<LinkedInContact> findAllByStatusAndLocationAndContactProcessingsIsNull(int status, Location location);

    LinkedInContact findFirstByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(int status, Location location, String role, String industries);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(int status, Location location, String role, String industries);

    LinkedInContact findFirstByStatusAndLocationAndIndustriesAndContactProcessingsIsNull(int status, Location location, String industries);

    List<LinkedInContact> findAllByStatusAndLocationAndIndustriesContainsAndRoleContains(int statusNew, Location location, String industries, String position);

    LinkedInContact findFirstByStatusAndLocationAndIndustriesContains(int status, Location location, String industries);

    LinkedInContact findFirstByStatusAndLocationAndIndustriesContainsAndRoleContains(int status, Location location, String industries, String role);

    LinkedInContact getById(Long id);

    @Query("select c.id " +
            "from LinkedInContact c " +
            "join ContactProcessing processing on c.id = processing.contact.id " +
            "join Account a on processing.account.id = a.id " +
            "where a.username = :username " +
            "order by c.id")
    Page<Long> getAvailableContact(@Param("username") String username, Pageable pageable);

    public boolean existsLinkedInContactByFirstNameAndLastNameAndCompanyName(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("companyName") String companyName);

    LinkedInContact getFirstByFirstNameAndLastName(String firstName, String lastName);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContains(int statusNew, Location location, String position);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNull(int statusNew, Location location, String position);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContainsAndIndustriesIsNullAndContactProcessingsIsNull(int statusNew, Location location, String position);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContainsAndIndustriesContains(int statusNew, Location location, String position, String industries);

    List<LinkedInContact> findAllByStatusAndLocationAndIndustriesContainsAndContactProcessingsIsNull(int statusNew, Location location, String industries);

    List<LinkedInContact> findAllByStatusAndLocationAndIndustriesIsNullAndContactProcessingsIsNull(int statusNew, Location location);

    List<LinkedInContact> findAllByStatusAndLocationAndIndustriesContains(int statusNew, Location location, String industries);

    List<LinkedInContact> findAllByStatusAndLocationAndIndustriesIsNull(int statusNew, Location location);

    List<LinkedInContact> findAllByStatusAndLocationAndRoleContainsAndContactProcessingsIsNull(int statusNew, Location location, String position);

    List<LinkedInContact> findAllByStatusAndLocation(int statusNew, Location location);

    Page<LinkedInContact> getAllByAssignments(Assignment assignment, Pageable pageable);
}
