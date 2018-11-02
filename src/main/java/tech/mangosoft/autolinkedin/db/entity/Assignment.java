package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;

import javax.annotation.PostConstruct;
import javax.persistence.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
//
//    public static final int TASK_DO_NOTHING = 0;
//    public static final int TASK_GRABBING = 1;
//    public static final int TASK_CONNECTION = 2;
//
//    public static final int STATUS_NEW = 0;
//    public static final int STATUS_IN_PROGRESS = 1;
//    public static final int STATUS_SUSPENDED = 2;
//    public static final int STATUS_ERROR = 32;
//    public static final int STATUS_FINISHED = 16;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private Integer page;

    @Column
    private Task task;

    @Transient
    private CompanyHeadcount companyHeadcount;

    @Column
    private Status status;

    @Column(length = 4096)
    private String params;

    @Column(name = "error_message", length = 4096)
    private String errorMessage;

    private String fullLocationString;

    private String position;

    private Integer countsFound;

    private Integer countMessages;

    private String industries;

    @Column(name = "message", length = 16000)
    private String message;

    private Date dailyLimitUpdateDate;

    private int dailyLimit = 0;

    @Column(name = "update_time")
    @LastModifiedDate
    @Temporal(TemporalType.DATE)
    private Date updateTime;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ContactProcessing> contactProcessings = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment", cascade = CascadeType.ALL)
    @JsonProperty
    private List<ProcessingReport> processingReports = new ArrayList<>();

    @ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "assignment_contacts",
            joinColumns = { @JoinColumn(name = "assignment_id") },
            inverseJoinColumns = { @JoinColumn(name = "contacts_id") }
    )
    private Set<LinkedInContact> contacts = new HashSet<>();

    @ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "assignment_headcounts",
            joinColumns = { @JoinColumn(name = "assignment_id") },
            inverseJoinColumns = { @JoinColumn(name = "headcounts_id") }
    )
    private Set<CompanyHeadcount> headcounts = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE , CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "assignment_groups",
            joinColumns = {@JoinColumn(name = "assignment_id")},
            inverseJoinColumns = {@JoinColumn(name = "groups_id")}
    )
    private Set<Group> groups = new HashSet<>();

    @PostConstruct
    private void formatDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format.format(this.updateTime);
        try {
            this.updateTime = format.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    public Assignment(Task task, String fullLocationString, String position, String industries, Account account) {
        this.page = 0;
        this.task = task;
        this.status = Status.STATUS_NEW;
        this.industries = industries;
        this.fullLocationString = fullLocationString;
        this.position = position;
        this.account = account;
        this.dailyLimitUpdateDate = new Date();
    }

    public Assignment(Task task, String fullLocationString, String position, String industries, String message, Integer countMessages, Account account) {
        this.page = 0;
        this.task = task;
        this.status = Status.STATUS_NEW;
        this.industries = industries;
        this.fullLocationString = fullLocationString;
        this.position = position;
        this.account = account;
        this.message = message;
        this.countMessages = countMessages;
        this.dailyLimitUpdateDate = new Date();
    }

    public Assignment setTask(Task task) {
        this.task = task;
        return this;
    }

    public Assignment setStatus(Status status) {
        this.status = status;
        return this;
    }

//    public String getSavedParams() {
//        return savedParams;
//    }

//    public void setSavedParams(String savedParams) {
//        this.savedParams = savedParams;
//    }

    public Assignment setAccount(Account account) {
        this.account = account;
        return this;
    }

    public Integer getPage() {
        return page != null ? page : 0;
    }
    public Assignment setPage(int page) {
        this.page = page;
        return this;
    }

    public Assignment setFullLocationString(String fullLocationString) {
        this.fullLocationString = fullLocationString;
        return this;
    }

    public Assignment setPosition(String position) {
        this.position = position;
        return this;
    }

    public Assignment setIndustries(String industries) {
        this.industries = industries;
        return this;
    }

    public Assignment setCompanyHeadcount(CompanyHeadcount companyHeadcount) {
        this.companyHeadcount = companyHeadcount;
        return this;
    }

    public Assignment setDailyLimitUpdateDate(Date dailyLimitUpdateDate) {
        this.dailyLimitUpdateDate = dailyLimitUpdateDate;
        return this;
    }

    public Assignment setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
        return this;
    }

    public void addProcessinReport(ProcessingReport pr) {
        processingReports.add(pr);
        pr.setAssignment(this);
    }

    public void removeProcessinReport(ProcessingReport pr) {
        processingReports.remove(pr);
        pr.setAssignment(null);
    }

    public void addLinkedInContact(LinkedInContact lc) {
        contacts.add(lc);
    }

    public void removeLinkedInContact(LinkedInContact lc) {
        contacts.remove(lc);
    }

    public void addContactProcessing(ContactProcessing cp) {
        contactProcessings.add(cp);
        cp.setAssignment(this);
    }

    public void removeContactProcessing(ContactProcessing cp) {
        contactProcessings.remove(cp);
        cp.setAssignment(null);
    }

}
