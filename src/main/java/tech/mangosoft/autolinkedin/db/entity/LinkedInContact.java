package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "contact")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LinkedInContact {

    //    TODO delete ths status
    //new statuses
    public static final int STATUS_IMPORTED = 0;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_REQUIRE_LINKED_IN_URL_UPDATE = 2;
    public static final int STATUS_REQUIRE_EMAIL = 3;
    public static final int STATUS_REQUIRE_LOAD_FROM_OTHER_ACCOUNT = 33;
    public static final int STATUS_ACQUIRED = 5;
    //statuses to delete
    public static final int STATUS_PROCESSED = 4;
    public static final int STATUS_ERROR = 16;
    public static final int STATUS_PREPROCESS_NEEDED = 33;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_linkedin")
    private String companyLinkedin;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String industries;

    private String role;

    @Column(name = "person_linkedin")
    private String linkedin;

    private String email;

    @Transient
    private String message;

    @Column(columnDefinition = "tinyint(4) default " + STATUS_IMPORTED)
    private Integer status;

    @Column(name = "update_time")
    @UpdateTimestamp
    private Date updateTime;

    @Column(name = "comments", length = 16000)
    private String comments;

    @Column
    private Boolean grabbedEmail;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "headcount_id")
    private CompanyHeadcount headcount;

    @ManyToMany(mappedBy = "contacts", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
            fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Assignment> assignments = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contact", cascade = CascadeType.ALL)
    private List<ContactProcessing> contactProcessings;

    @Column(name = "creation_time")
    @Type(type = "java.sql.Timestamp")
    private Timestamp createTime;

    public LinkedInContact(String companyName, String companyWebsite, String companyLinkedin, String firstName, String lastName, String role, String linkedin, String email, Date updateTime) {
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.companyLinkedin = companyLinkedin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.linkedin = linkedin;
        this.email = email;
        this.updateTime = updateTime;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
        assignment.getContacts().remove(this);
    }
}


