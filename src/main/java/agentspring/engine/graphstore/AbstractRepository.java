package agentspring.engine.graphstore;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.graph.core.NodeBacked;
import org.springframework.data.graph.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.graph.neo4j.repository.GraphRepository;
import org.springframework.data.graph.neo4j.support.GraphDatabaseContext;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.gremlin.jsr223.GremlinScriptEngineFactory;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.Pipeline;
import com.tinkerpop.pipes.SingleIterator;

public abstract class AbstractRepository<T extends NodeBacked> {

    Logger logger = Logger.getLogger(AbstractRepository.class);

    ScriptEngine engine = null;
    
    @Autowired
    DirectGraphRepositoryFactory directGraphRepositoryFactory;
    
    @Autowired
    GraphDatabaseContext graphDatabaseContext;
    
    private GraphRepository<T> repository() {
        return directGraphRepositoryFactory.createGraphRepository(getActualType());
    }

    public Graph getGraph() {
        return new Neo4jGraph(this.getGraphDatabaseContext().getGraphDatabaseService());
    }

    private ScriptEngine getGremlinEngine() {
        if (engine == null) {
            engine = new GremlinScriptEngineFactory().getScriptEngine();
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("g", getGraph());
        }
        return engine;
    }

    /*
     * Finder methods
     */

    public Iterable<T> findAllByPropertyValue(String property, Object value) {
        return repository().findAllByPropertyValue(property, value);
    }

    public T findByPropertyValue(String property, Object value) {
        return repository().findByPropertyValue(property, value);
    }

    public Iterable<T> findAll() {
        return repository().findAll();
    }

    public Iterable<T> findAllAtRandom() {
        List<T> list = Utils.asList(findAll());
        Collections.shuffle(list, new Random());
        return list;
    }

    public T findById(long id) {
        return repository().findOne(id);
    }

    public long count() {
        return repository().count();
    }

    public <E extends NodeBacked> Iterable<T> findAllByTraversal(E startNode, TraversalDescription traversalDescription) {
        return repository().findAllByTraversal(startNode, traversalDescription);
    }

    public <E extends NodeBacked> Iterable<T> findAllByPipe(E startNode, Pipe<Vertex, Vertex> pipe) {
        Vertex startVertex = getVertex(startNode);
        Pipe<Vertex, T> typed = new MappingPipe();
        Pipe<Vertex, T> emit = new Pipeline<Vertex, T>(pipe, typed);
        emit.setStarts(new SingleIterator<Vertex>(startVertex));
        return emit;
    }

    public <E extends NodeBacked> Iterable<T> findAllByGremlin(E startNode, String gremlinExpression) {
        List<Vertex> results = new ArrayList<Vertex>();
        getGremlinEngine().getBindings(ScriptContext.ENGINE_SCOPE).put("v", getVertex(startNode));
        getGremlinEngine().getBindings(ScriptContext.ENGINE_SCOPE).put("results", results);
        try {
            engine.eval(gremlinExpression + " >> results");
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
        Pipe<Vertex, T> pipe = new MappingPipe();
        pipe.setStarts(results);
        return pipe;
    }
    
    public DirectGraphRepositoryFactory getDirectGraphRepositoryFactory() {
        return directGraphRepositoryFactory;
    }

    public void setDirectGraphRepositoryFactory(DirectGraphRepositoryFactory directGraphRepositoryFactory) {
        this.directGraphRepositoryFactory = directGraphRepositoryFactory;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getActualType() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public GraphDatabaseContext getGraphDatabaseContext() {
        return graphDatabaseContext;
    }

    public void setGraphDatabaseContext(GraphDatabaseContext graphDatabaseContext) {
        this.graphDatabaseContext = graphDatabaseContext;
    }

    public <E extends NodeBacked> Vertex getVertex(E e) {
        return getGraph().getVertex(e.getNodeId());
    }

    class MappingPipe extends AbstractPipe<Vertex, T> implements Pipe<Vertex, T> {
        @Override
        protected T processNextStart() throws NoSuchElementException {
            Vertex v = this.starts.next();
            return repository().findOne((Long) v.getId());
        }
    }

}
