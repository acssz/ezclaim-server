package org.acssz.ezclaim.repository;

import org.acssz.ezclaim.domain.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClaimRepository extends MongoRepository<Claim, String> {
}

