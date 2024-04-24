package com.reactivespringboot.reactor.app;


import com.reactivespringboot.reactor.app.models.dao.ProductoDao;
import com.reactivespringboot.reactor.app.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;



@SpringBootApplication
@EnableMongoRepositories
public class AppApplication implements CommandLineRunner {

	private final ProductoDao productoDao;

	private static final Logger log = LoggerFactory.getLogger(AppApplication.class);

    public AppApplication(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		Flux.just(
				Producto.builder().nombre("TV Panasonic").precio(456.89).build(),
				Producto.builder().nombre("Sony Camara HD Digital").precio(177.89).build(),
				Producto.builder().nombre("Apple ipod").precio(46.89).build(),
				Producto.builder().nombre("Sony Notebook").precio(846.89).build(),
				Producto.builder().nombre("Hewlett Packard Multifuncional").precio(200.89).build(),
				Producto.builder().nombre("Bianchi Bicicleta").precio(70.89).build(),
				Producto.builder().nombre("HP Notebook Omen 17").precio(2500.89).build(),
				Producto.builder().nombre("Mica Cómoda 5 Cajones").precio(150.89).build(),
				Producto.builder().nombre("TV Sony Bravia OLED 4k Ultra HD").precio(2255.89).build()

		).flatMap(producto -> productoDao.save(producto)) // No sirve porque retornaria un Mono de producto. flatMap sinos retorna los productos !!!
				.subscribe(producto -> log.info("Insert :" + producto.getId() +" "+ producto.getNombre()));
	}
}
