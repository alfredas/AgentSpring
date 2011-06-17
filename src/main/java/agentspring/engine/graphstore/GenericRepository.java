package agentspring.engine.graphstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.graph.core.NodeBacked;
import org.springframework.data.graph.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.graph.neo4j.repository.GraphRepository;
import org.springframework.data.graph.neo4j.support.GraphDatabaseContext;
import org.springframework.stereotype.Repository;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.gremlin.jsr223.GremlinScriptEngineFactory;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.Pipeline;
import com.tinkerpop.pipes.SingleIterator;

@Repository
public class GenericRepository {

    Logger logger = Logger.getLogger(GenericRepository.class);

    ScriptEngine engine = null;

    DirectGraphRepositoryFactory directGraphRepositoryFactory;

    @Autowired
    GraphDatabaseContext graphDatabaseContext;
    
    private <T extends NodeBacked> GraphRepository<T> repository(Class<T> clazz) {
        if (directGraphRepositoryFactory == null) {
            directGraphRepositoryFactory = new DirectGraphRepositoryFactory(graphDatabaseContext);
        }
        return directGraphRepositoryFactory.createGraphRepository(clazz);
    }

    public <T extends NodeBacked> Iterable<T> findAllByPropertyValue(Class<T> clazz, String property, Object value) {
        return repository(clazz).findAllByPropertyValue(property, value);
    }

    public <T extends NodeBacked> T findByPropertyValue(Class<T> clazz, String property, Object value) {
        return repository(clazz).findByPropertyValue(property, value);
    }

    public <T extends NodeBacked> Iterable<T> findAll(Class<T> clazz) {
        return repository(clazz).findAll();
    }

    public <T extends NodeBacked> Iterable<T> findAllAtRandom(Class<T> clazz) {
        List<T> list = Utils.asList(findAll(clazz));
        Collections.shuffle(list, new Random());
        return list;
    }

    public <T extends NodeBacked> T findFirst(Class<T> clazz) {
        if (repository(clazz).findAll().iterator().hasNext()) {
            return repository(clazz).findAll().iterator().next();
        } else {
            return null;
        }
    }

    public <T extends NodeBacked> T findById(Class<T> clazz, long id) {
        return repository(clazz).findOne(id);
    }

    public <T extends NodeBacked, E extends NodeBacked> Iterable<T> findAllByTraversal(Class<T> clazz, E startNode,
            TraversalDescription traversalDescription) {
        return repository(clazz).findAllByTraversal(startNode, traversalDescription);
    }

    public <T extends NodeBacked> T createEntityFromState(Class<T> clazz, Node node) {
        return graphDatabaseContext.createEntityFromState(node, clazz);
    }

    public Graph getGraph() {
        return new Neo4jGraph(this.getGraphDatabaseContext().getGraphDatabaseService());
    }

    public <T extends NodeBacked, E extends NodeBacked> Iterable<T> findAllByPipe(Class<T> clazz, E startNode, Pipe<Vertex, Vertex> pipe) {
        Vertex startVertex = getVertex(startNode);
        Pipe<Vertex, T> typed = new MappingPipe<T>(clazz);
        Pipe<Vertex, T> emit = new Pipeline<Vertex, T>(pipe, typed);
        emit.setStarts(new SingleIterator<Vertex>(startVertex));
        return emit;
    }

    private ScriptEngine getGremlinEngine() {
        if (engine == null) {
            engine = new GremlinScriptEngineFactory().getScriptEngine();
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("g", getGraph());
        }
        return engine;
    }

    public <T extends NodeBacked, E extends NodeBacked> Iterable<T> findAllByGremlin(Class<T> clazz, E startNode, String gremlinExpression) {
        List<Vertex> results = new ArrayList<Vertex>();
        getGremlinEngine().getBindings(ScriptContext.ENGINE_SCOPE).put("v", getVertex(startNode));
        getGremlinEngine().getBindings(ScriptContext.ENGINE_SCOPE).put("results", results);
        try {
            engine.eval(gremlinExpression + " >> results");
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
        Pipe<Vertex, T> pipe = new MappingPipe<T>(clazz);
        pipe.setStarts(results);
        return pipe;
    }

    public GraphDatabaseContext getGraphDatabaseContext() {
        return graphDatabaseContext;
    }

    public void setGraphDatabaseContext(GraphDatabaseContext graphDatabaseContext) {
        this.graphDatabaseContext = graphDatabaseContext;
    }

    public <T extends NodeBacked> Vertex getVertex(T t) {
        return getGraph().getVertex(t.getNodeId());
    }

    class MappingPipe<T extends NodeBacked> extends AbstractPipe<Vertex, T> implements Pipe<Vertex, T> {

        Class<T> genericClass;

        public MappingPipe(Class<T> clazz) {
            super();
            genericClass = clazz;
        }

        @Override
        protected T processNextStart() throws NoSuchElementException {
            Vertex v = this.starts.next();
            return findById(genericClass, (Long) v.getId());
        }

    }

}
