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
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}


}
