package tech.mangosoft.autolinkedin.controller.messages;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class GraphMessage {

    private List<String> labels;
    private List<Integer[]> series;

    private List<String> accounts;

}
