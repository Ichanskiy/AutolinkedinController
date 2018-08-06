package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.mangosoft.autolinkedin.db.entity.enums.Role;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column( length = 50 )
    private String first;

    @Column( length = 50 )
    private String last;

    @Column( length = 50, unique = true)
    private String username;

    @Column( length = 50 )
    private String password;

    @Column( name = "execution_limit" )
    private Integer executionLimit;

    @Column( name = "grabbing_limit" )
    private Integer grabbingLimit;

    @Column( name = "last_page" )
    private Integer lastPage;

    private Role role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private List<ContactProcessing> contactProcessings;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Company company;

    public Account() {
    }

    public Account(String first, String last, String username, String password, Integer executionLimit, Integer grabbingLimit) {
        this.first = first;
        this.last = last;
        this.username = username;
        this.password = password;
        this.executionLimit = executionLimit;
        this.grabbingLimit = grabbingLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getExecutionLimit() {
        return executionLimit;
    }

    public Account setExecutionLimit(Integer executionLimit) {
        this.executionLimit = executionLimit;
        return this;
    }

    public Integer getGrabbingLimit() {
        return grabbingLimit;
    }

    public void setGrabbingLimit(Integer grabbingLimit) {
        this.grabbingLimit = grabbingLimit;
    }

    public Integer getLastPage() {
        return lastPage;
    }

    public Account setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
        return this;
    }

    public String getCaption() {
        return first + " " + last;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Role getRole() {
        return role;
    }

    public Account setRole(Role role) {
        this.role = role;
        return this;
    }

    public void setContactProcessings(List<ContactProcessing> contactProcessings) {
        this.contactProcessings = contactProcessings;
    }
}
