package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentsByParam {

    @JsonProperty
    private String email;

    @JsonProperty
    private Integer status;

    @JsonProperty
    private String from;

    @JsonProperty
    private String to;
}
