package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrabbingMessage {

    @JsonProperty
    private String login;

    @JsonProperty
    private String fullLocationString;

    @JsonProperty
    private String position;

    @JsonProperty
    private String industries;

    public GrabbingMessage() {
    }

    public GrabbingMessage(String login, String fullLocationString, String position, String industries) {
        this.login = login;
        this.fullLocationString = fullLocationString;
        this.position = position;
        this.industries = industries;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullLocationString() {
        return fullLocationString;
    }

    public void setFullLocationString(String fullLocationString) {
        this.fullLocationString = fullLocationString;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getIndustries() {
        return industries;
    }

    public void setIndustries(String industries) {
        this.industries = industries;
    }
}
