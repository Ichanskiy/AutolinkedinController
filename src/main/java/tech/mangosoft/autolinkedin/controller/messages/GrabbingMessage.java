package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrabbingMessage {

    @JsonProperty
    private String login;

    @JsonProperty
    private String fullLocationString;

    @JsonProperty
    private String position;

    @JsonProperty
    private String industries;

    @JsonProperty
    private List<Long> idsHeadcount;

    @JsonProperty
    private Long groupId;

    public GrabbingMessage(String login, String fullLocationString, String position, String industries) {
        this.login = login;
        this.fullLocationString = fullLocationString;
        this.position = position;
        this.industries = industries;
    }

}
