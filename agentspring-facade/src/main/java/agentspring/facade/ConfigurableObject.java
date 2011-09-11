package agentspring.facade;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigurableObject implements Serializable {

    private String id;
    private String clazz;
    private Map<String, ScenarioParameter> parameters = new HashMap<String, ScenarioParameter>();

    private static final long serialVersionUID = 1L;

    public ConfigurableObject(String id, String clazz) {
        super();
        this.id = id;
        this.clazz = clazz;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Collection<ScenarioParameter> getParameters() {
        return this.parameters.values();
    }

    public void addParameter(ScenarioParameter scenarioParameter) {
        this.parameters.put(scenarioParameter.getField(), scenarioParameter);
    }

    public ScenarioParameter getParameter(String field) {
        return this.parameters.get(field);
    }

    public void setParamValue(String field, Object value) {
        this.parameters.get(field).setValue(value);
    }

    public boolean containsParam(String field) {
        return this.parameters.containsKey(field);
    }
}
