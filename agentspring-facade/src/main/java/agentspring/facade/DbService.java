package agentspring.facade;

import java.util.List;

import javax.script.ScriptException;

public interface DbService {
    public List<Object> executeGremlinQueries(String gremlinQuery, String nodeType) throws ScriptException;

    public Object executeGremlinQuery(String gremlinQuery) throws ScriptException;

    public List<String> getStartNodes();

    public void setFilters(Filters filters);

}
