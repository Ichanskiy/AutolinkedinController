package tech.mangosoft.autolinkedin.controller.messages;

import java.util.List;

public class GraphMessage {

    private List<String> labels;
    private List<Integer[]> series;

    private List<String> accounts;

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

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }
}
