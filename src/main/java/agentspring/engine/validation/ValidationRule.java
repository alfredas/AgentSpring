package agentspring.engine.validation;

public interface ValidationRule {

    public void validate();

    public String getAfter();

    public void setAfter(String after);

    public String getBefore();

    public void setBefore(String before);

}
