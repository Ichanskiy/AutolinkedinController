package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.LastModifiedDate;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;


@Entity
@Table(name = "assignment")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Assignment {

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

    private String industries;

    private String position;

    private String fullLocationString;

    private Integer countsFound;

    private Integer countMessages;

    private Date dailyLimitUpdateDate;

    private int dailyLimit = 0;

    @Column(name = "error_message", length = 4096)
    private String errorMessage;

    @Column(name = "message", length = 16000)
    private String message;

    @Column(name = "update_time")
    @LastModifiedDate
    @Temporal(TemporalType.DATE)
    private Date updateTime;

    @ManyToOne
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

    @ManyToMany(cascade = {CascadeType.MERGE , CascadeType.PERSIST}, fetch = FetchType.LAZY)
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

}
