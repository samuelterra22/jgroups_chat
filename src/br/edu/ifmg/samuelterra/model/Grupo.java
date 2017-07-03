package br.edu.ifmg.samuelterra.model;


import org.jgroups.Address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/***********************************************************************************************************************
 *                                         JGroups Chat v1.0                                                           *
 *   Alunos:        Matheus Calixto | Samuel Terra                                                                     *
 *   Professor:     Everthon Valadão                                                                                   *
 *   Disciplina:    Sistemas Distribuidos                                                                              *
 *   Modificado em: 03/07/2017                                                                                         *
 *                                                                                                                     *
 **********************************************************************************************************************/
public class Grupo implements Serializable {

    private String nome;

    private Usuario coordenador;

    private List<Usuario> usuarios;

    public Grupo() {
        usuarios = new ArrayList<>();
    }

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
        return this.usuarios;
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

    public int numeroDeUsuarios(){
        return this.usuarios.size();
    }

    public boolean contemUsuario(Usuario usuario){
        for (Usuario u : usuarios) {
            if (u.getAddress() == usuario.getAddress()){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Grupo{" +
                "nome='" + nome + '\'' +
                ", coordenador=" + coordenador +
                ", usuarios=" + usuarios +
                '}';
    }
}


//keytool -genseckey -alias myKey -keypass changeit -storepass changeit -keyalg Blowfish -keysize 56 -keystore defaultStore.keystore -storetype JCEKS