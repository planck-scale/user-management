package com.tericcabrel.authorization.repositories;

import com.tericcabrel.authorization.models.entities.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(value = "com.tericcabrel.authorization.repositories.UserRepository")
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);
}
