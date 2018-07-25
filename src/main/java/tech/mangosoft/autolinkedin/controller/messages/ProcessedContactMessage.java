package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessedContactMessage {

    @JsonProperty
    private Long assignmentId;

    @JsonProperty
    private String login;

    @JsonProperty
    private Integer page;

    public ProcessedContactMessage() {
    }

    public ProcessedContactMessage(Long assignmentId, String login, Integer page) {
        this.assignmentId = assignmentId;
        this.login = login;
        this.page = page;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
