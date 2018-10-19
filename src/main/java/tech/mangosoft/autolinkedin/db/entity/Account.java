package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty
    @Column( length = 50 )
    private String first;

    @JsonProperty
    @Column( length = 50 )
    private String last;

    @JsonProperty
    @Column( length = 50, unique = true)
    private String username;

    @JsonProperty
    @Column( length = 50 )
    private String password;

//    @JsonProperty
//    @Column( name = "execution_limit" )
//    private Integer executionLimit;

    @JsonProperty
    @Column( name = "grabbing_limit" )
    private Integer grabbingLimit;

    @JsonProperty
    @Column( name = "last_page" )
    private Integer lastPage;

    @JsonProperty
    private Role role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private List<ContactProcessing> contactProcessings;

    @JsonProperty()
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    public Account() {
    }

    public Account(String first, String last, String username, String password, Integer grabbingLimit) {
        this.first = first;
        this.last = last;
        this.username = username;
        this.password = password;
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

//    public Integer getExecutionLimit() {
//        return executionLimit;
//    }
//
//    public Account setExecutionLimit(Integer executionLimit) {
//        this.executionLimit = executionLimit;
//        return this;
//    }

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

    public boolean isAdmin(){
        return (this.getRole() != null) && this.getRole().equals(Role.ADMIN);
    }
}
