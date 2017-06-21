package br.edu.ifmg.samuelterra.model;


import br.edu.ifmg.samuelterra.controller.Usuario;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {

    private String nome;

    private List<Usuario> usuarios;

    public Grupo(String nome, List<Usuario> usuarios) {
        this.nome = nome;
        this.usuarios = usuarios;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public void adicionaUsuario(Usuario u){
        this.usuarios.add(u);
    }
}
