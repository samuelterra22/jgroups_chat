package br.edu.ifmg.samuelterra.model;

import br.edu.ifmg.samuelterra.controller.Usuario;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private String mensagem;
    private Usuario remetente;
    private Usuario destinatario;
    private String hora;


    public Mensagem(Usuario destinatario, Usuario remetente, String mensagem, String hora) {
        this.destinatario = destinatario;
        this.remetente = remetente;
        this.mensagem = mensagem;
        this.hora = hora;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Usuario getRemetente() {
        return remetente;
    }

    public void setRemetente(Usuario remetente) {
        this.remetente = remetente;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }
}
