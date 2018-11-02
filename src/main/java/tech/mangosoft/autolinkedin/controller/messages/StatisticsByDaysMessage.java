package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsByDaysMessage {

    @JsonProperty
    private Account account;

    @JsonProperty
    private List<LinkedInContact> grabbingContacts;

    @JsonProperty
    private List<LinkedInContact> connectedContacts;

    public StatisticsByDaysMessage setAccount(Account account) {
        this.account = account;
        return this;
    }
}
