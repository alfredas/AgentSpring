package agentspring.graphdb;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import agentspring.facade.Filters;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;

/**
 * Default implementation of the filters that expose functionality to gremlin
 * queries on the front-end
 * 
 * @author alfredas
 * 
 */
public class DefaultFiltersImpl implements Filters {

    @Autowired
    Neo4jTemplate template;

    @SuppressWarnings("unused")
    private NodeBacked getEntity(Object node) {
        if (!(node instanceof Neo4jVertex))
            throw new RuntimeException("Object is not neo4j vertex");
        Neo4jVertex vertex = (Neo4jVertex) node;
        Node n = vertex.getRawVertex();
        NodeBacked entity = template.createEntityFromStoredType(n);
        return entity;
    }

    @Override
    public void init() {
    }
}