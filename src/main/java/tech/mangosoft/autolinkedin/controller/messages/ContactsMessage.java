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
public class ContactsMessage {

    @JsonProperty
    @Setter
    private String location;

    @JsonProperty
    @Setter
    private String industries;

    @JsonProperty
    @Setter
    private String position;

    @JsonProperty
    @Setter
    private Integer page;

    @JsonProperty
    @Setter
    private Long userId;

}
