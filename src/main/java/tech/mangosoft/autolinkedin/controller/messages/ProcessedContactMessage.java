package tech.mangosoft.autolinkedin.controller.messages;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProcessedContactMessage {

    private Long assignmentId;

    private Integer status;

    private String login;

    private Integer page;

}
