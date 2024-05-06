package com.reactivespringboot.reactor.app.controllers;

import com.reactivespringboot.reactor.app.models.dao.ProductoDao;
import com.reactivespringboot.reactor.app.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoDao productoDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    public ProductoRestController(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @GetMapping()
    public Flux<Producto> index(){

        return productoDao.findAll()
                .map(producto ->  {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return  producto;
                }).doOnNext(producto -> log.info(producto.getNombre()));
    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id){

        /* En aras de practicar se retornaá todos los productos y luego si filtrará y convertirá a Mono
        * Así se haria directo : return productoDao.findById(id);
        * */
        // .next vaa convertir el objeto en Mono !!
       return productoDao.findAll().filter(producto -> producto.getId().equals(id)).next()
               .doOnNext(producto -> log.info(producto.getNombre()));
    }
}
