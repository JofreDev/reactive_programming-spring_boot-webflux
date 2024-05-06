package com.reactivespringboot.reactor.app.controllers;

import com.reactivespringboot.reactor.app.AppApplication;
import com.reactivespringboot.reactor.app.models.dao.ProductoDao;
import com.reactivespringboot.reactor.app.models.documents.Categoria;
import com.reactivespringboot.reactor.app.models.documents.Producto;
import com.reactivespringboot.reactor.app.models.services.ProductoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Controller
/*Los datos de este objeto estarán persistentes de manera automatica hastaq ue se guarde
* gracias al 'SessionStatus sessionStatus' pasado como argumento.
* Por lo tanto el id siempre va a estar presente, solo se podran modificar los campos presentes
* en el formulario  */
@SessionAttributes("producto") // cada vez que se pase el objeto producto se guarda en la sesión http
//@AllArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    private final String ruta;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    public ProductoController(ProductoService productoService, @Value("${configuration.path}") String ruta) {
        this.productoService = productoService;
        this.ruta = ruta;
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id){

        return productoService.findById(id)
                .doOnNext( producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("titulo", "Detalle del producto");
                })
                .flatMap(p -> Mono.just("ver"))
                .switchIfEmpty(Mono.error(new InterruptedException("No existe el producto")))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    /*Mono<ResponseEntity<Resource>> : Componente spring que nos permite guardar contenido en el cuerpo de la respuesta
    * -> En el responseBody, el contenido puede ser un json o xml, etc o una imagen
    * como vamos a retornar una imagen entonces usamos 'Resource' */
    @GetMapping("/uploads/img/{nombreFoto:.+}") // '.+' -> Es una expresi[on regular para concatenar, en este caso la extensión de la foto
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {

        Path rutaImagen = Paths.get(this.ruta).resolve(nombreFoto).toAbsolutePath();
        Resource imagen = new UrlResource(rutaImagen.toUri());
        log.info("La ruta de la imagen es : "+rutaImagen.toUri());

        return Mono.just(ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""+imagen.getFilename()+ "\"")
                .body(imagen));

    }

    @ModelAttribute("categorias") // Asi se guarda en la vista para referenciar en formulario
    public Flux<Categoria> categorias(){
        return productoService.findAllCategoria();
    }

    @GetMapping({"/listar","/"})

    public Mono<String> listar(Model model) {

        Flux<Producto> productos = productoService.findAllWithNameUpperCase()
                .doOnNext(producto -> log.info(producto.getNombre()));

        model.addAttribute("productos", productos);

        model.addAttribute("titulo", "Listado de Productos");

        return Mono.just("listar") ;

    }

    @GetMapping("/form")
    public Mono<String> crear(Model model){

        model.addAttribute("producto", Producto.builder().build());
        model.addAttribute("titulo", "Formulario de producto");
        model.addAttribute("boton", "Crear producto");
        return Mono.just("form");
    }


    /* @PathVariable(name = "id") String idProducto -> Homologamos el parametro del get con el de la función
    * Se usa en caso de que los nombres sean distintos. De lo contrario solo usar :
    * @PathVariable String id
    *
    * Model model -> Para pasar el Mono<Producto a la vista> */
    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable(name = "id") String idProducto, Model model){

        Mono<Producto> productoMono = productoService.findById(idProducto)
                .doOnNext(producto -> log.info("- Producto : "+producto.getNombre()))
                // Si el Mono es vacio, entonces...
                .defaultIfEmpty(Producto.builder().build());

        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", productoMono);

        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2Reactivo(@PathVariable(name = "id") String idProducto, Model model){

        ///De esta forma no se puede manejar la sesión, solo se puede usar el hidden !
        return productoService.findById(idProducto)
                .doOnNext(producto ->  {
                    log.info("- Producto : "+producto.getNombre());
                    model.addAttribute("boton", "Editar");
                    model.addAttribute("titulo", "Editar Producto");
                    model.addAttribute("producto", producto);
                })
                .defaultIfEmpty(Producto.builder().build())
                .flatMap(producto -> {
                    if (producto.getId()== null){
                        //Envolver en Mono
                        return Mono.error(new InterruptedException("No existe el producto")); // captura el error
                    }
                    // Importante envovler en Mono al producto para retornar
                    return Mono.just(producto);
                })
                .then(Mono.just("form")) // Si todo sale bien ejecuta el .then
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto")); // Si falla arrida en el if, se va al onErrorResume

    }


    /* Se realiza el mapeo de formulario con objeto desde el formulario th :
    <div>
    <form th:action="@{/form}" method="post" th:object="${producto}">
        <div><label for="nombre"}>Nombre</label>
            <div><input type="text" th:field="*{nombre}"></div>
        </div>
        <div><label for="precio">Precio</label>
            <div><input type="number" th:field="*{precio}></div>
        </div>
        <div><label for="createAt">Fecha</label>
            <div><input type="date" th:field="*{createAt}></div>
        </div>
            <div><input type="submit"></div>
        </div>
    </form>
</div>
     */
    @PostMapping("/form")
    //@Valid para que evalue la integridad del objeto especificada en la entidad
    // Deben ir en este orden : @Valid Producto producto, BindingResult bindingResult (Objeto-entidad , Binding)
    /* @ModelAttribute("producto") -> la idea es que el nombre de la entidad sea igual al nombre con el
     * que guardamos la entidad en el model : model.addAttribute("producto", producto);
     * si no es asi usar : @ModelAttribute("producto")
     * */

    public Mono<String> guardar(@Valid /*@ModelAttribute("producto")*/ Producto producto, BindingResult bindingResult,
                                Model model, SessionStatus sessionStatus,@RequestPart FilePart file){

        if (bindingResult.hasErrors()){
            model.addAttribute("titulo", "Errores en formulario productos");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        }else{
            sessionStatus.setComplete(); // Finaliza el proceso y se limpia la sesión !!

            /// El producto solo tiene el id de categoria, por lo tanto hay que buscar el objeto categoria completo
            /// y pasarlo al producto para poder guardarlo

            return productoService.findCategoriaById(producto.getCategoria().getId())
                    .flatMap(categoria -> {
                        if(producto.getCreateAt()==null){
                            producto.setCreateAt(new Date());
                        }

                        if (!file.filename().isEmpty())
                            producto.setFoto(UUID.randomUUID().toString() +"-"+ file.filename()
                                    .replace(" ","")
                                    .replace(":","")
                                    .replace("\\","")
                            );
                        producto.setCategoria(categoria);
                        return productoService.save(producto);
                    }).doOnNext( p -> log.info("producto guardado : " + p.getNombre()+" Id :"+ p.getId()+ " Categoria "+ p.getCategoria().getNombre()))
                    .flatMap(producto1 -> {
                        if (!file.filename().isEmpty()){

                            return file.transferTo(new File(ruta+producto1.getFoto()));
                        }
                        return Mono.empty();
                    })
                     .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id){
        return productoService.findById(id)
                .defaultIfEmpty(Producto.builder().build()) // Si retorna un nulo entonces se crea un producto vacio por defecto
                .flatMap(producto -> {
                    if (producto.getId()== null){
                        //Envolver en Mono
                        return Mono.error(new InterruptedException("No existe el producto a eliminar")); // captura el error
                    }
                    // Importante envovler en Mono al producto para retornar
                    return Mono.just(producto);
                })
                .flatMap(p-> {
                    log.info("Eliminando el producto : "+ p.getNombre()+ ", con Id : "+p.getId());
                    return  productoService.delete(p);
                }).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar")); // Si falla arrida en el if, se va al onErrorResume;
    }

    // Contrapresión, Ideal con un delay
    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {

        Flux<Producto> productos = productoService.findAllWithNameUpperCase().delayElements(Duration.ofSeconds(1))
                .doOnNext(producto -> log.info(producto.getNombre()));

        // Inducimos en las lineas anteriores un delay.
        // Por eso seria buena opción manejar la contrapresión 'back-pressure' con 'ReactiveDataDriverContextVariable'
        // buffer medido en cantidad de elementos !!!
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos,2 ));

        model.addAttribute("titulo", "Listado de Productos");

        return "listar";

    }


    @GetMapping("/listar-full")

    public String listarFull(Model model) {

        Flux<Producto> productos = productoService.findAllWithNameUpperCaseRepeat(); // se repetirá la operación 5000 veces !!

        model.addAttribute("productos", productos);

        model.addAttribute("titulo", "Listado de Productos");

        return "listar";

    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Producto> productos = productoService.findAllWithNameUpperCaseRepeat(); // se repetirá la operación 5000 veces !!

        model.addAttribute("productos", productos);

        model.addAttribute("titulo", "Listado de Productos");

        return "listar-chunked";

    }

}
