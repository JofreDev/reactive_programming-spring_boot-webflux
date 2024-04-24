package com.reactivespringboot.reactor.app.models.dao;

import com.reactivespringboot.reactor.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/*Objetivo: Clase ProductDao para las operaciones de la BD,
*           podemos tener metodos personalizados, que hagan
*           consultas u operaciones.
*           ReactiveMongoRepository<Entidad, Tipo de dato del id>
*/
public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
}
