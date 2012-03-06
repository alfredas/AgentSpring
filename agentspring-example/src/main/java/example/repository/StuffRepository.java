package example.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import example.domain.agent.ExampleAgent;
import example.domain.things.Stuff;

public interface StuffRepository extends GraphRepository<Stuff> {

    @Query(value = "g.v(agent).out('OWN')", type = QueryType.Gremlin)
    public Iterable<Stuff> findMyStuff(@Param("agent") ExampleAgent agent);

}
