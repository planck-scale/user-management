package com.tericcabrel.authorization.repositories;

import com.tericcabrel.authorization.models.entities.UserAccount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends MongoRepository<UserAccount, ObjectId> {
    Optional<UserAccount> findByToken(String token);
}
