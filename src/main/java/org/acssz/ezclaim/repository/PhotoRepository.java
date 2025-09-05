package org.acssz.ezclaim.repository;

import org.acssz.ezclaim.domain.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoRepository extends MongoRepository<Photo, String> {}
