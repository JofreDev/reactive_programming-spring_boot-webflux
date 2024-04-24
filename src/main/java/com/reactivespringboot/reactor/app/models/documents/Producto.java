package com.reactivespringboot.reactor.app.models.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/*Objetivo :  Mapeamos la colección de la bd de productos
*             por tal motivo usamos la API de mongo para spring.
*             La anotación convertirá la clase en formato BSON
*             (Binary JSON) que es el formato que usa mongo.
*               */
@Document(collection = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

   @Id // Es una llave
    private String id;

   // Atributos del documento

    private String nombre;
    private Double precio;
    private Date createAt;

    public Producto(String nombre, Double precio) {
        super();
        this.nombre = nombre;
        this.precio = precio;
    }
}
