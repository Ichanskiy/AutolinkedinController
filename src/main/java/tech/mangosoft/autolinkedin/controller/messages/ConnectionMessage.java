package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionMessage {

    @JsonProperty
    private String login;

    @JsonProperty
    private String location;

    @JsonProperty
    private String fullLocationString;

    @JsonProperty
    private String position;

    @JsonProperty
    private String industries;

    @JsonProperty
    private String message;

    public ConnectionMessage() {
    }

    public ConnectionMessage(String login, String location, String fullLocationString, String position, String industries, String message) {
        this.login = login;
        this.location = location;
        this.fullLocationString = fullLocationString;
        this.position = position;
        this.industries = industries;
        this.message = message;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
