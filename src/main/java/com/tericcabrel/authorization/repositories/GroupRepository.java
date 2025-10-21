package com.tericcabrel.authorization.repositories;

import com.tericcabrel.authorization.models.entities.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByPath(String path);
    Optional<Group> findByName(String name);
}

