package agentspring.service;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.NodeBacked;
import org.springframework.data.neo4j.support.GraphDatabaseContext;

import agentspring.facade.Filters;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;

public class DefaultFiltersImpl implements Filters {
    @Autowired
    GraphDatabaseContext graphDatabaseContext;

    private NodeBacked getEntity(Object node) {
        if (!(node instanceof Neo4jVertex))
            throw new RuntimeException("Object is not neo4j vertex");
        Neo4jVertex vertex = (Neo4jVertex) node;
        Node n = vertex.getRawVertex();
        NodeBacked entity = graphDatabaseContext.createEntityFromStoredType(n);
        return entity;
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }
}