package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(columnDefinition = "int default 0")
    private Integer page;

    @Column
    private Task task;

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

    public Assignment() {
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public Assignment setTask(Task task) {
        this.task = task;
        return this;
    }

    public Assignment setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

//    public String getSavedParams() {
//        return savedParams;
//    }

//    public void setSavedParams(String savedParams) {
//        this.savedParams = savedParams;
//    }

    public Status getStatus() {
        return status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Account getAccount() {
        return account;
    }

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFullLocationString() {
        return fullLocationString;
    }

    public Assignment setFullLocationString(String fullLocationString) {
        this.fullLocationString = fullLocationString;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public Assignment setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getIndustries() {
        return industries;
    }

    public Assignment setIndustries(String industries) {
        this.industries = industries;
        return this;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getDailyLimitUpdateDate() {
        return dailyLimitUpdateDate;
    }

    public Assignment setDailyLimitUpdateDate(Date dailyLimitUpdateDate) {
        this.dailyLimitUpdateDate = dailyLimitUpdateDate;
        return this;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCountsFound() {
        return countsFound;
    }

    public Integer getCountMessages() {
        return countMessages;
    }

    public void setCountMessages(Integer countMessages) {
        this.countMessages = countMessages;
    }

    public void setCountsFound(Integer countsFound) {
        this.countsFound = countsFound;
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

    public List<ContactProcessing> getContactProcessings() {
        return contactProcessings;
    }

    public List<ProcessingReport> getProcessingReports() {
        return processingReports;
    }

    public Set<CompanyHeadcount> getHeadcounts() {
        return headcounts;
    }

    public void setHeadcounts(Set<CompanyHeadcount> headcounts) {
        this.headcounts = headcounts;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }
}
