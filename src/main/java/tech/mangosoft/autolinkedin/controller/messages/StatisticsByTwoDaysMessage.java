package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

import java.util.List;

public class StatisticsByTwoDaysMessage {

    @JsonProperty
    private Account account;

    @JsonProperty
    private List<LinkedInContact> grabbingContacts;

    @JsonProperty
    private List<LinkedInContact> connectedContacts;

    public StatisticsByTwoDaysMessage() {
    }

    public StatisticsByTwoDaysMessage(Account account, List<LinkedInContact> grabbingContacts, List<LinkedInContact> connectedContacts) {
        this.account = account;
        this.grabbingContacts = grabbingContacts;
        this.connectedContacts = connectedContacts;
    }

    public Account getAccount() {
        return account;
    }

    public StatisticsByTwoDaysMessage setAccount(Account account) {
        this.account = account;
        return this;
    }

    public List<LinkedInContact> getGrabbingContacts() {
        return grabbingContacts;
    }

    public void setGrabbingContacts(List<LinkedInContact> grabbingContacts) {
        this.grabbingContacts = grabbingContacts;
    }

    public List<LinkedInContact> getConnectedContacts() {
        return connectedContacts;
    }

    public void setConnectedContacts(List<LinkedInContact> connectedContacts) {
        this.connectedContacts = connectedContacts;
    }
}
