package com.reactivespringboot.reactor.app.models.dao;

import com.reactivespringboot.reactor.app.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
