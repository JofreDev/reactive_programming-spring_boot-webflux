package com.reactivespringboot.reactor.app.model;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {

    public List<String> getComentarios() {
        return comentarios;
    }

    private List<String> comentarios;

    public Comentarios(List<String> comentarios) {
        this.comentarios = new ArrayList<>();
    }

    public Comentarios() {
        this.comentarios = new ArrayList<>();
    }

    public void addComentario(String comentario) {
        this.comentarios.add(comentario) ;
    }

    @Override
    public String toString() {
        return "Comentarios{" +
                "comentarios=" + comentarios +
                '}';
    }
}
