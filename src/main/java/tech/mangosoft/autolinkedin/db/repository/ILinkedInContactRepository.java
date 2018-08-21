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



    LinkedInContact findFirstByStatusAndRoleContainsAndContactProcessingsIsNull(int status, String role);

    LinkedInContact findFirstByStatusAndLocationAndRoleContainsAndIndustriesContainsAndContactProcessingsIsNull(int status, Location location, String role, String industries);

    Page<LinkedInContact> countAllByLocationAndIndustriesContainsAndRoleContaining(Location location, String industries, String position, Pageable pageable);

    Long countAllByLocationAndRoleContainsAndCreateTimeBetween(Location location, String role, Date after, Date before);

    List<LinkedInContact> findAllByLocationAndRoleContains(Location location, String industries, Pageable pageable);

    Long countAllByLocationAndRoleContains(Location location, String industries);

    List<LinkedInContact> findAllByLocation(Location location, Pageable pageable);

    List<LinkedInContact> findAllByLocation(Location location);

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

    @Query("select c.companyName, c.firstName, c.lastName, c.role, c.linkedin, l.location, c.industries, c.email from LinkedInContact c join Location l on c.location = l.id  where  l.id = :locationId")
    List getContactsToCsv(@Param("locationId") Long locationId);
}
