package br.edu.ifmg.samuelterra.model;


import br.edu.ifmg.samuelterra.controller.Usuario;
import org.jgroups.Address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Grupo implements Serializable {

    private String nome;

    private Usuario coordenador;

    private List<Usuario> usuarios;

    public Grupo(String nome, Usuario coordenador, List<Usuario> usuarios) {
        this.nome = nome;
        this.coordenador = coordenador;
        this.usuarios = usuarios;
    }

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

    public Usuario getCoordenador() {
        return coordenador;
    }

    public void setCoordenador(Usuario coordenador) {
        this.coordenador = coordenador;
    }

    public List<Address> getEnderecos(){

        List<Address> enderecos = new ArrayList<>();

        for (Usuario u : this.usuarios) {
            enderecos.add(u.getAddress());
        }
        return enderecos;
    }

}
