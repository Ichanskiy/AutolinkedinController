package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

public class StatisticsByConnectionMessage {

    @JsonProperty
    private Assignment assignment;

    @JsonProperty
    private Page<LinkedInContact> connectedContacts;

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Page<LinkedInContact> getConnectedContacts() {
        return connectedContacts;
    }

    public void setConnectedContacts(Page<LinkedInContact> connectedContacts) {
        this.connectedContacts = connectedContacts;
    }
}

