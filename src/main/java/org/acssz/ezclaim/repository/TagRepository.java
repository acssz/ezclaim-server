package org.acssz.ezclaim.repository;

import org.acssz.ezclaim.domain.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TagRepository extends MongoRepository<Tag, String> {}

