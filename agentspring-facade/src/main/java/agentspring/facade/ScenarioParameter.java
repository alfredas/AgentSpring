package agentspring.facade;

import java.io.Serializable;

public class ScenarioParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private Object value;
    private String label;
    private Double from = null;
    private Double to = null;
    private Double step = null;

    public ScenarioParameter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public void setFrom(Double form) {
        this.from = form;
    }

    public void setTo(Double to) {
        this.to = to;
    }

    public ScenarioParameter(String field, Object value, String label) {
        this(field, value);
        this.label = label;
    }

    public ScenarioParameter(String field, Object value, String label, Double from, Double to) {
        this(field, value, label);
        this.from = from;
        this.to = to;
    }

    public ScenarioParameter(String field, Object value, String label, Double from, Double to, Double step) {
        super();
        this.field = field;
        this.value = value;
        this.label = label;
        this.from = from;
        this.to = to;
        this.step = step;
    }

    public Double getFrom() {
        return from;
    }

    public Double getTo() {
        return to;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

}
