package com.fatec.monitoria.modules.monitor.domain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MonitorRepository extends MongoRepository<Monitor, String> {
    Optional<Monitor> findByEmailIgnoreCase(String email);
}
