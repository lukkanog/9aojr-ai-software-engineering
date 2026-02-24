package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "seed_metadata")
public class SeedMetadata {

    @Id
    private String id;

    @Indexed(unique = true)
    private String chave;

    private Instant aplicadoEm;

    public SeedMetadata() {
    }

    public SeedMetadata(String chave, Instant aplicadoEm) {
        this.chave = chave;
        this.aplicadoEm = aplicadoEm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public Instant getAplicadoEm() {
        return aplicadoEm;
    }

    public void setAplicadoEm(Instant aplicadoEm) {
        this.aplicadoEm = aplicadoEm;
    }
}
