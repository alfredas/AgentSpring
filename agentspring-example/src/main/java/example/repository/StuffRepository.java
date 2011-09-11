package example.repository;

import org.springframework.data.neo4j.repository.GraphRepository;

import example.domain.things.Stuff;

public interface StuffRepository extends GraphRepository<Stuff> {
}
