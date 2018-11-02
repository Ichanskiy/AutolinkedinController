package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedContactMessage {

    @JsonProperty
    private Long assignmentId;

    @JsonProperty
    private String login;

    @JsonProperty
    private Integer page;
}
