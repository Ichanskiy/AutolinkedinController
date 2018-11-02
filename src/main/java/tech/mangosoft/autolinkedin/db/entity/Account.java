package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mangosoft.autolinkedin.db.entity.enums.Role;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public Account(String first, String last, String username, String password, Integer grabbingLimit) {
        this.first = first;
        this.last = last;
        this.username = username;
        this.password = password;
        this.grabbingLimit = grabbingLimit;
    }

//    public Integer getExecutionLimit() {
//        return executionLimit;
//    }
//
//    public Account setExecutionLimit(Integer executionLimit) {
//        this.executionLimit = executionLimit;
//        return this;
//    }

    public Account setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
        return this;
    }

    public String getCaption() {
        return first + " " + last;
    }


    public Account setRole(Role role) {
        this.role = role;
        return this;
    }


    public boolean isAdmin(){
        return (this.getRole() != null) && this.getRole().equals(Role.ADMIN);
    }
}
