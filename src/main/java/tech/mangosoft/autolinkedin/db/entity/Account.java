package tech.mangosoft.autolinkedin.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import tech.mangosoft.autolinkedin.db.entity.enums.Role;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Account {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JsonProperty
    @Column(length = 50)
    private String first;

    @JsonProperty
    @Column(length = 50)
    private String last;

    @JsonProperty
    @Column(length = 50, unique = true)
    private String username;

    @JsonProperty
    @Column(length = 50)
    private String password;

    @JsonProperty
    @Column(name = "grabbing_limit")
    private Integer grabbingLimit;

    @JsonProperty
    @Column(name = "last_page")
    private Integer lastPage;

    private boolean confirm;

    @JsonProperty
    private Role role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private List<ContactProcessing> contactProcessings;

    @JsonProperty()
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    public String getCaption() {
        return first + " " + last;
    }
    public boolean isAdmin() {
        return (this.getRole() != null) && this.getRole().equals(Role.ADMIN);
    }
}
