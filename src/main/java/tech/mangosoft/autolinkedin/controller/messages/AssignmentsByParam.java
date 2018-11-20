package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
