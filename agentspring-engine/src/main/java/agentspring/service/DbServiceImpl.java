package agentspring.service;

import java.math.BigDecimal;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyPermission;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import agentspring.facade.DbService;
import agentspring.facade.Filters;
import agentspring.graphdb.NodeEntityHelper;
import agentspring.simulation.Schedule;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/** 
 * Gremlin query execution backend.
 * @author alfredas
 *
 */
public class DbServiceImpl implements DbService {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(DbServiceImpl.class);

    private ScriptException exception = null;

    private Object result = null;

    private AccessControlContext evalContext = null;

    @Autowired
    Neo4jTemplate template;

    Filters filters;

    @Autowired
    NodeEntityHelper nodeEntityHelper;

    public DbServiceImpl() {
        Permissions perms = new Permissions();
        perms.add(new RuntimePermission("accessDeclaredMembers.*"));
        perms.add(new PropertyPermission("line.separator", "read"));
        ProtectionDomain domain = new ProtectionDomain(new CodeSource(null, (Certificate[]) null), perms);
        evalContext = new AccessControlContext(new ProtectionDomain[] { domain });
    }

    @Override
    public List<Object> executeGremlinQueries(String nodeType, String gremlinQuery) throws ScriptException {
        ScriptEngine engine = getScriptEngine();

        List<Object> result = new ArrayList<Object>();
        if (nodeType == null) {
            result.add(this.executeQuery(gremlinQuery, null, engine));
        } else {
            List<Vertex> startNodes = new ArrayList<Vertex>();
            startNodes = (List<Vertex>) engine.eval("g.idx('__types__')[[className:'" + nodeEntityHelper.getNodeEntityMap().get(nodeType)
                    + "']].toList()");
            for (Vertex v : startNodes) {
                result.add(this.executeQuery(gremlinQuery, v, engine));
            }
        }

        return result;
    }

    @Override
    public Object executeGremlinQuery(String gremlinQuery) throws ScriptException {
        ScriptEngine engine = getScriptEngine();
        return this.executeQuery(gremlinQuery, null, engine);
    }

    private Object entityRepresentation(Object entity) {
        if (entity instanceof Vertex) {
            HashMap<String, Object> vertex = new HashMap<String, Object>();
            HashMap<String, Object> properties = new HashMap<String, Object>();
            Vertex v = (Vertex) entity;
            for (String key : v.getPropertyKeys()) {
                properties.put(key, v.getProperty(key));
            }
            vertex.put("properties", properties);
            return vertex;
        } else if (entity instanceof Edge) {
            // type = RepresentationType.RELATIONSHIP;
            // results.add(new RelationshipRepresentation(((Neo4jEdge)
            // r).getRawEdge()));
        } else if (entity instanceof Graph) {
            // type = RepresentationType.STRING;
            // results.add(ValueRepresentation.string(graph.getRawGraph().toString()));
        } else if (entity instanceof Double || entity instanceof Float) {
            return ((Number) entity).doubleValue();
        } else if (entity instanceof Long || entity instanceof Integer) {
            return ((Number) entity).longValue();
        } else if (entity instanceof BigDecimal) {
            return ((BigDecimal) entity).doubleValue();
        } else if (entity == null) {
            return null;
        } else if (entity instanceof Iterable<?>) {
            List<Object> representation = new ArrayList<Object>();
            for (final Object r : (Iterable<?>) entity) {
                representation.add(this.entityRepresentation(r));
            }
            return representation;
        }
        return entity.toString();
    }

    private Object executeQuery(final String gremlinQuery, Vertex startNode, final ScriptEngine engine) throws ScriptException {
        if (startNode != null) {
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("v", startNode);
        }
        exception = null;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    result = engine.eval(gremlinQuery);
                } catch (ScriptException e) {
                    exception = e;
                }
                return null;
            }
        }, evalContext);
        if (exception != null) {
            throw new ScriptException("from doPriv: " + exception.getMessage());
        }

        if (result instanceof Iterable<?>) {
            final List<Object> results = new ArrayList<Object>();
            for (final Object r : (Iterable<?>) result) {
                results.add(this.entityRepresentation(r));
            }
            return results;
        } else {
            return this.entityRepresentation(result);
        }
    }

    private ScriptEngine getScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("gremlin-groovy");
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("g", new Neo4jGraph(this.template.getGraphDatabaseService()));
        filters.init();
        Nodes n = new Nodes(engine);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("f", filters);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("n", n);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("tick", Schedule.getSchedule().getCurrentTick());
        return engine;
    }

    @Override
    public List<String> getStartNodes() {
        return new ArrayList<String>(nodeEntityHelper.getNodeEntityMap().keySet());
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    class Nodes {
        ScriptEngine engine;

        public Nodes(ScriptEngine engine) {
            this.engine = engine;
        }

        public List<Vertex> getNodes(String type) throws ScriptException {
            return (List<Vertex>) engine.eval("g.idx('__types__')[[className:'" + nodeEntityHelper.getNodeEntityMap().get(type)
                    + "']].toList()");
        }
    }

}
