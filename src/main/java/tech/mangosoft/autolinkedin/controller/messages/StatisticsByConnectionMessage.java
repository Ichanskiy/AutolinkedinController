package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsByConnectionMessage {

    @JsonProperty
    private Assignment assignment;

    @JsonProperty
    private Page<LinkedInContact> connectedContacts;

}

