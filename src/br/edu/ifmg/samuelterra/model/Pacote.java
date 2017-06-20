package br.edu.ifmg.samuelterra.model;


import org.jgroups.Address;
import org.jgroups.Message;

import java.io.Serializable;
import java.util.Map;

public class Pacote implements Serializable {

    private Mensagem mensagem;
    private Map<String, Address> listaDeContatos;
    private Tag tag;

    public Pacote(Mensagem mensagem, Map<String, Address> listaDeContatos, Tag tag) {
        this.mensagem = mensagem;
        this.listaDeContatos = listaDeContatos;
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
}
