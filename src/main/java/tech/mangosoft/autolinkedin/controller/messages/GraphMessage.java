package tech.mangosoft.autolinkedin.controller.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMessage {

    private List<String> labels;

    private List<Integer[]> series;

    private List<String> accounts;
}
