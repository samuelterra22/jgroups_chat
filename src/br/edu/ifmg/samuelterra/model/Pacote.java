package br.edu.ifmg.samuelterra.model;


import br.edu.ifmg.samuelterra.controller.Usuario;
import org.jgroups.Address;
import org.jgroups.Message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Pacote implements Serializable {

    private Mensagem mensagem;
    private Map<String, Address> listaDeContatos;
    private Map<String, Grupo>  listDeGrupos;
    private Tag tag;

    public Pacote(Mensagem mensagem, Map<String, Address> listaDeContatos, Map<String, Grupo>  listDeGrupos, Tag tag) {
        this.mensagem = mensagem;
        this.listaDeContatos = listaDeContatos;
        this.listDeGrupos = listDeGrupos;
        this.tag = tag;
    }

    public Mensagem getMensagem() {
        return mensagem;
    }

    public void setMensagem(Mensagem mensagem) {
        this.mensagem = mensagem;
    }

    public Map<String, Address> getListaDeContatos() {
        return listaDeContatos;
    }

    public void setListaDeContatos(Map<String, Address> listaDeContatos) {
        this.listaDeContatos = listaDeContatos;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Map<String, Grupo>  getListDeGrupos() {
        return listDeGrupos;
    }

    public void setListDeGrupos(Map<String, Grupo>  listDeGrupos) {
        this.listDeGrupos = listDeGrupos;
    }
}
