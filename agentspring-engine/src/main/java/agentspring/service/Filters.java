package agentspring.service;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.NodeBacked;
import org.springframework.data.neo4j.support.GraphDatabaseContext;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;
import com.tinkerpop.gremlin.jsr223.GremlinScriptEngine;

public class Filters {
    @Autowired
    GraphDatabaseContext graphDatabaseContext;
    
    @Autowired
    NodeEntityHelper nodeEntityHelper;

    private ScriptEngine engine;
    private List<Vertex> startNodes;
    

    public void init() {
        this.engine = new GremlinScriptEngine();
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("g",
                new Neo4jGraph(this.graphDatabaseContext.getGraphDatabaseService()));
    }

    public List<Vertex> getNodes(String type) throws ScriptException {
        this.startNodes = new ArrayList<Vertex>();
        this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("nodes", startNodes);
        this.engine.eval("g.idx('__types__')[[className:'" + nodeEntityHelper.getNodeEntityMap().get(type) + "']] >> nodes");
        return startNodes;
    }

    @SuppressWarnings("unused")
    private NodeBacked getEntity(Object node) {
        if (!(node instanceof Neo4jVertex))
            throw new RuntimeException("Object is not neo4j vertex");
        Neo4jVertex vertex = (Neo4jVertex) node;
        Node n = vertex.getRawVertex();
        NodeBacked entity = graphDatabaseContext.createEntityFromStoredType(n);
        return entity;
    }

}
