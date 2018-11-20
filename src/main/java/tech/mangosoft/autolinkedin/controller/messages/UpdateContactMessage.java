package tech.mangosoft.autolinkedin.controller.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UpdateContactMessage {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String email;

    @JsonProperty
    private String industries;

    @JsonProperty
    private String comment;

    @JsonProperty
    private String location;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private String role;

    @JsonProperty
    private String companyName;

    @JsonProperty
    private String companySite;

}
