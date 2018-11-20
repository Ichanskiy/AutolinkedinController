package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class StatisticsByDaysMessage {

    @JsonProperty
    private Account account;

    @JsonProperty
    private List<LinkedInContact> grabbingContacts;

    @JsonProperty
    private List<LinkedInContact> connectedContacts;
}
