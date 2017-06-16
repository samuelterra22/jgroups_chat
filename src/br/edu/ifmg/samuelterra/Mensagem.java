package br.edu.ifmg.samuelterra;

import java.io.Serializable;

/**
 * Created by samuel on 15/06/17.
 */
public class Mensagem implements Serializable {

    private String mensagem;
    private String remetente;
    private String hora;

    public Mensagem(String remetente, String mensagem, String hora) {
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
