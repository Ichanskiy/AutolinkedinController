package tech.mangosoft.autolinkedin.controller.messages;

public class ProcessedContactMessage {


    private Long assignmentId;

    private Integer status;

    private String login;

    private Integer page;

    public ProcessedContactMessage() {
    }

    public ProcessedContactMessage(Long assignmentId, Integer status, String login, Integer page) {
        this.assignmentId = assignmentId;
        this.status = status;
        this.login = login;
        this.page = page;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
