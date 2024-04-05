package com.reactivespringboot.reactor.app;

import ch.qos.logback.core.spi.PreSerializationTransformer;
import com.reactivespringboot.reactor.app.model.Comentarios;
import com.reactivespringboot.reactor.app.model.Usuario;
import com.reactivespringboot.reactor.app.model.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.management.MemoryNotificationInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		//exampleIterable();
		//exampleFlatMap();
		//exampleToString();
		//exampleConvertFuxToMono();
		/* exampleUserCommentsFlatMap == exampleUserCommentsZipWith == exampleUserCommentsZipWith
		* Todos hacen la misma tarea con diferentes operadores*/
			//exampleUserCommentsFlatMap();
			//exampleUserCommentsZipWith();
			//example2UserCommentsZipWith();
		//exampleZipWithRanges();

		/* ***********        exampleInterval vs exampleDelayElements        ***********
		* En resumen, ambos operadores trabajan con retrasos de tiempo, pero interval está diseñado para tareas periódicas
		*  y constantes, mientras que delayElements es para introducir un retraso fijo entre elementos. Ambos pueden
		* compartir hilos de un pool común para optimizar recursos. */

		//exampleInterval(); // interval usa un hilo de temporizador compartido para emitir ticks a intervalos regulares.
		//exampleDelayElements(); // delayElements probablemente usará un hilo de temporizador para cada suscriptor,

		//exampleInfinitiveInterval();
		//exampleIntervalFromCreate();
		exampleBackPressure();


	}

	public void exampleBackPressure() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux.range(1,10)
				// Podemos observar como por defecto el suscriptor solicita la maxima cantidad de info posible de elementos
				.log() // Podemos ver la traza de nuestro flux.
				.limitRate(5)
				// forma manual
				.subscribe(/* new Subscriber<Integer>() {

					private Subscription subscription;
					private Integer limit = 5; // Tamaño de los lotes a enviar

					private Integer consumed = 0;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.subscription = subscription;
						subscription.request(limit);

					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consumed++;
						if(consumed == limit){
							consumed =0;
							subscription.request(limit);
						}
					}

					@Override
					public void onError(Throwable throwable) {

					}

					@Override
					public void onComplete() {

					}
				} */);


	}

	public void exampleIntervalFromCreate() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux.create(emitter -> {
			Timer timer = new Timer();
			/* schedule(task,firstTime,timePeriod)
			* Comienza en un segundo y se repite cada un segundo */
			timer.schedule(new TimerTask() {

				private Integer cont = 0;
				@Override
				public void run() {
					emitter.next(++cont);
					if(cont == 10) {
						timer.cancel();
						emitter.complete(); // Quiere decir que se completo con satisfacción
					}

				/*	if(cont == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el Flux en 5"));
					} */

				}
			}, 1000, 1000);
		})//.doOnNext(next -> log.info(next.toString()))
		//.doOnComplete(() -> log.info("Ha finalizado")) // detecta el evento de finalización. Solo si todo salio bien.
		.subscribe(next -> log.info(next.toString()),
				error -> log.error(error.getMessage()),
				() -> log.info("Hemos terminado")); // Tercer argumento funciona como "doOnComplete"


	}

	public void exampleInfinitiveInterval() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux.interval(Duration.ofSeconds(1))
				.flatMap(i -> {
					if(i >=5 ){
						return Flux.error(new InterruptedException("Solo hasta 5 "));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola "+i)
				.retry(2 ) // Intentará ejecutar n veces de nuevo si sucede un error !!!
				//.doOnNext(log::info)
				//Manera más elegante
				.subscribe(log::info, e-> log.error(e.getMessage()));


	}

	public void exampleDelayElements() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux.range(1,12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()))
				.subscribe();

	}


	public void exampleInterval() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux<Integer> range = Flux.range(1,12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (r,d) -> r)
				.doOnNext(i -> log.info(i.toString())) // doOnNext aplica una tarea a los elementos. Lo mismo que pasar la logica al subscribe
				.subscribe()
				//.blockLast() // Suscribe al flujo con bloqueo
		;
	}

	public void exampleZipWithRanges() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Flux.just(1,2,3,4)
				.map( x -> x*2) // Aplicamos transformación
				// firstFlow -> Flux.just(1,2,3,4) & secondFlow -> Flux.range(0,4)
				// Creamos un flux de enteros con range
				// Queremos convertirlos a un string que muestre ambos datos
				.zipWith(Flux.range(0,4), (firstFlow, secondFlow) ->
						String.format("primer Flux : %d , Segundo Flux : %d", firstFlow,secondFlow) )
				.subscribe(log::info);

	}

	public void example2UserCommentsZipWith() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Mono<Usuario> usuarioMono = Mono.fromCallable( () -> {
			return new Usuario("John", "Doe");
		});

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, que tal");
			comentarios.addComentario("Ejemplo comentario");
			comentarios.addComentario("prueba prueba prueba");
			comentarios.addComentario("Fin de los comentarios");
			return comentarios;
		});
		// Tenemos en principio que trabajar con flatMap para aplanar la otra lista de comentarios
		// En fusiones usamos flat map
		// zipWith(referencia de "usuarioMono", referencia de "comentariosUsuarioMono")
		usuarioMono.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u,c);
				})
				.subscribe(uc -> log.info(uc.toString()));

	}

	public void exampleUserCommentsZipWith() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Mono<Usuario> usuarioMono = Mono.fromCallable( () -> {
			return new Usuario("John", "Doe");
		});

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, que tal");
			comentarios.addComentario("Ejemplo comentario");
			comentarios.addComentario("prueba prueba prueba");
			comentarios.addComentario("Fin de los comentarios");
			return comentarios;
		});
		// Tenemos en principio que trabajar con flatMap para aplanar la otra lista de comentarios
		// En fusiones usamos flat map
		// zipWith(referencia de "usuarioMono", referencia de "comentariosUsuarioMono")
		usuarioMono.zipWith(comentariosUsuarioMono, (u,c)-> new UsuarioComentarios(u,c))
				.subscribe(uc -> log.info(uc.toString()));

	}

	/* Para el siguiente ejercicio se busca :
	* combinar 2 flujos con el operador flatMap
	* un flujo tiene usuarios y el otro con comentarios
	* El tipo final del flujo seria UsuarioConComentarios */

	public void exampleUserCommentsFlatMap() throws Exception {

		// Alternativa para crear un "Mono" llamando a otro metodo
		Mono<Usuario> usuarioMono = Mono.fromCallable( () -> {
			return new Usuario("John", "Doe");
		});

		List<Usuario> userList = new ArrayList<>();
		userList.add(new Usuario("Andres","Guzman"));
		userList.add(new Usuario("Pedro","Fulano"));

		Flux<Usuario> usuariosFlux = Flux.fromIterable(userList);

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, que tal");
			comentarios.addComentario("Ejemplo comentario");
			comentarios.addComentario("prueba prueba prueba");
			comentarios.addComentario("Fin de los comentarios");
			return comentarios;
		});
		// Tenemos en principio que trabajar con flatMap para aplanar la otra lista de comentarios
		// En fusiones usamos flat map
		usuarioMono.flatMap( u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u,c)))
				.subscribe(uc -> log.info(uc.toString()));

		// Para estas tareas es mejor iniciar con FlatMap !!! y despues usar map
		usuariosFlux.flatMap(u -> comentariosUsuarioMono.map(
				comentarios -> {
					if(!u.getNombre().equalsIgnoreCase("Pedro") && comentarios.getComentarios().contains("Hola, que tal")){
						UsuarioComentarios userComments = new UsuarioComentarios(u,comentarios);
						return userComments;
					}
					UsuarioComentarios userComments = new UsuarioComentarios(u,null);
					return userComments;
				}
				))
				.subscribe(uc -> log.info(uc.toString()));


	}


	public void exampleConvertFuxToMono() throws Exception {

		List<Usuario> userList = new ArrayList<>();
		userList.add(new Usuario("Andres","Guzman"));
		userList.add(new Usuario("Pedro","Fulano"));
		userList.add(new Usuario("Maria","Fulana"));
		userList.add(new Usuario("Diego","Sultano"));
		userList.add(new Usuario("Juan","Mengano"));
		userList.add(new Usuario("Bruce","Lee"));
		userList.add(new Usuario("Bruce","Willis"));

		// Conversión de un Flux a Mono mediante [ collectList() ]
		Flux.fromIterable(userList)
				.collectList() // Convierte el Flux a un Mono
				.subscribe(list -> list.forEach(item-> log.info(item.toString())));


	}

	public void exampleToString() throws Exception {

		List<Usuario> userList = new ArrayList<>();
		userList.add(new Usuario("Andres","Guzman"));
		userList.add(new Usuario("Pedro","Fulano"));
		userList.add(new Usuario("Maria","Fulana"));
		userList.add(new Usuario("Diego","Sultano"));
		userList.add(new Usuario("Juan","Mengano"));
		userList.add(new Usuario("Bruce","Lee"));
		userList.add(new Usuario("Bruce","Willis"));

		// Queremos convertir a String mediante un map [Se modifica todos los elementos del Flux de manera independiente]
		// por eso se usa Map. No es necesario crear otro observables (no es necesario usar flatMap)

		Flux.fromIterable(userList)
				.map( user -> user.getNombre().toUpperCase().concat(" ").concat(user.getApellido().toUpperCase()))
				// usamos flatMap porque estamos filtrando
				.flatMap( name -> {
					if(name.contains("bruce".toUpperCase()))
						return Mono.just(name) ; // Debe retornar un Observable !!!
					return Mono.empty();  // retorna un Mono vacio !!
				}) // acá ya no se necesita usar Mono's, vamos a usar map para modificar todos los elementos y por lo tanto retornamos el obj
				.map( user -> {
					String nombre =  user.toLowerCase();
					return nombre ;
				}).subscribe(u -> log.info(u.toString()));


	}

	public void exampleFlatMap() throws Exception {

		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andres Guzman");
		nombresList.add("Pedro Fulano");
		nombresList.add("Maria Fulana");
		nombresList.add("Diego Sultano");
		nombresList.add("Juan Mengano");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");


		Flux.fromIterable(nombresList)
				.map((String nombre) -> new Usuario(nombre.split(" ")[0].toUpperCase(),
						nombre.split(" ")[1].toUpperCase()))
				.flatMap( user -> {
					if(user.getNombre().equalsIgnoreCase("bruce"))
						return Mono.just(user) ; // Debe retornar un Observable !!!
					return Mono.empty();  // retorna un Mono vacio !!
				})
				.map( user -> {
					String nombre =  user.getNombre().toLowerCase();
					user.setNombre(nombre);
					return user ;
				}).subscribe(u -> log.info(u.toString()));


	}

	public void exampleIterable() throws Exception {

		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andres Guzman");
		nombresList.add("Pedro Fulano");
		nombresList.add("Maria Fulana");
		nombresList.add("Diego Sultano");
		nombresList.add("Juan Mengano");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");

		// Creando primer "flux" / "observable"
		// Flux es una clase abstracta que implementa de CorePublisher
		// Just para inicializar con o sin valores el flux
		//Flux<String> nombres = Flux.just("Andres Guzman","Pedro Fulano" ,"Maria Fulana", "Diego Sultano","Juan Mengano","Bruce Lee", "Bruce Willis" );

		// Se crea un flux a partir de un iterable.
		Flux<String> nombres = Flux.fromIterable(nombresList);
		/*Si no sé trabaja directamente con el flujo, no sé efectuan nuevos cambios. Al usar la referencia de "nombres"
		 * se retornaria un nuevo Flux, mas no se modifica el original "nombres". Por lo tanto evidenciamos que los observables
		 * son inmutables*/
		// este ya es un Flux nuevo !!!
		Flux<Usuario> nombresUser =nombres.map((String nombre) -> new Usuario(nombre.split(" ")[0].toUpperCase(),
						nombre.split(" ")[1].toUpperCase()))
				.filter( user -> user.getNombre().toLowerCase().equals("bruce"))

				//.doOnNext(System.out::println); Ejemplo 1
				// Ahora se quiere emular un error
				.doOnNext(e ->  {
					if(e==null){

						throw  new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(e.getNombre().concat(" ").concat( e.getApellido()));
				}).map( user -> {
					String nombre =  user.getNombre().toLowerCase();
					user.setNombre(nombre);
					return user ;
				}) ;

		/* doOnNext -> Es un método evento que parte del ciclo de vida del observable, cada vez que llega un elemento
		 *
		 */

		//La logica tanto del subscribe como del doOnNext() al final se ejecuta en el .doOnNext()
		//Suscribimos nuestro observador con una tarea (subscribe con consumidor ) Ejemplo1
		//nombres.subscribe(log::info); // Se realiza suscripción para ejecutar .doOnNext()
		// Ejemplo 2
		//nombres.subscribe(log::info, error -> log.error(error.getMessage()));

		// Ejemplo 3 (con 3 parametros [consumer lambda, manejo de error y runnable])
		nombresUser.subscribe(e -> log.info(e.toString()) , error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecución del observable con éxito");
					}
				});


	}
}
