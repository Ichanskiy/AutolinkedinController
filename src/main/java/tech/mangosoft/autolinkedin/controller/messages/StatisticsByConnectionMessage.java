package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

import java.util.List;

public class StatisticsByConnectionMessage {

    @JsonProperty
    private Assignment assignment;

    @JsonProperty
    private List<LinkedInContact> connectedContacts;

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public List<LinkedInContact> getConnectedContacts() {
        return connectedContacts;
    }

    public void setConnectedContacts(List<LinkedInContact> connectedContacts) {
        this.connectedContacts = connectedContacts;
    }
}
