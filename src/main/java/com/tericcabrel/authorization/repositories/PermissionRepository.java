package com.tericcabrel.authorization.repositories;

import com.tericcabrel.authorization.models.entities.Permission;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends MongoRepository<Permission, ObjectId> {
    Optional<Permission> findByName(String name);
}
