package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class LinkedInContact {
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

    public LinkedInContact() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public LinkedInContact setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public LinkedInContact setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
        return this;
    }

    public Boolean getGrabbedEmail() {
        return grabbedEmail;
    }

    public LinkedInContact setGrabbedEmail(Boolean grabbedEmail) {
        this.grabbedEmail = grabbedEmail;
        return this;
    }

    public String getCompanyLinkedin() {
        return companyLinkedin;
    }

    public void setCompanyLinkedin(String companyLinkedin) {
        this.companyLinkedin = companyLinkedin;
    }

    public String getFirstName() {
        return firstName;
    }

    public LinkedInContact setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public LinkedInContact setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getRole() {
        return role;
    }

    public LinkedInContact setRole(String role) {
        this.role = role;
        return this;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<Assignment> assignments) {
        this.assignments = assignments;
    }

    public String getEmail() {
        return email;
    }

    public LinkedInContact setEmail(String email) {
        this.email = email;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public LinkedInContact setStatus(Integer status) {
        this.status = status;
        return this;
    }


    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


    public String getIndustries() {
        return industries;
    }

    public LinkedInContact setIndustries(String industries) {
        this.industries = industries;
        return this;
    }

    public CompanyHeadcount getHeadcount() {
        return headcount;
    }

    public LinkedInContact setHeadcount(CompanyHeadcount headcount) {
        this.headcount = headcount;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public LinkedInContact setLocation(Location location) {
        this.location = location;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<ContactProcessing> getContactProcessings() {
        return contactProcessings;
    }

    public void setContactProcessings(List<ContactProcessing> contactProcessings) {
        this.contactProcessings = contactProcessings;
    }

    public String getComments() {
        return comments;
    }

    public LinkedInContact setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
        assignment.getContacts().remove(this);
    }
}


