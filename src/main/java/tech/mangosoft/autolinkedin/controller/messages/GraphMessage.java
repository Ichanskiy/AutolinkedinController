package tech.mangosoft.autolinkedin.controller.messages;

import java.util.List;

public class GraphMessage {

    private List<String> labels;
    private List<Integer[]> series;

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Integer[]> getSeries() {
        return series;
    }

    public void setSeries(List<Integer[]> series) {
        this.series = series;
    }
}
