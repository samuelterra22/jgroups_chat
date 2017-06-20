package br.edu.ifmg.samuelterra.model;

import java.io.Serializable;

public class Mensagem implements Serializable {

    private String mensagem;
    private String remetente;
    private String destinatario;
    private String hora;


    public Mensagem(String mensagem, String remetente, String hora) {
        this.mensagem = mensagem;
        this.remetente = remetente;
        this.hora = hora;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
