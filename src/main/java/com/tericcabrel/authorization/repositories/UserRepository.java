package com.tericcabrel.authorization.repositories;

import com.tericcabrel.authorization.models.entities.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository(value = "com.tericcabrel.authorization.repositories.UserRepository")
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);

    @Query("{ 'groupPaths' : { $elemMatch: { $regex: ?0 } } }")
    List<User> findByGroupPathRegex(String pathRegex);

    Optional<User> findByEmailAndTenantId(String email, String tenantId);
}
