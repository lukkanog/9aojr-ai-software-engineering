package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.SeedMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeedMetadataRepository extends MongoRepository<SeedMetadata, String> {
    boolean existsByChave(String chave);
}
