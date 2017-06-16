package br.edu.ifmg.samuelterra;

import java.io.Serializable;

/**
 * Created by samuel on 15/06/17.
 */
public class Mensagem implements Serializable {

    private String mensagem;
    private String remetente;
    private String destinatario;
    private String hora;
    private Tag tag;

    public Mensagem(String mensagem, String remetente, String hora, Tag tag) {
        this.mensagem = mensagem;
        this.remetente = remetente;
        this.hora = hora;
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
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
