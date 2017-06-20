package br.edu.ifmg.samuelterra.controller;

import br.edu.ifmg.samuelterra.model.Mensagem;
import br.edu.ifmg.samuelterra.model.Pacote;
import br.edu.ifmg.samuelterra.model.Tag;
import org.jgroups.Address;
import org.jgroups.Message;

import java.util.Map;


public class CriaMensagem {

    private String conteudo;
    private Message pacoteMulti, pacoteUni, pacoteAny;

    public CriaMensagem() {
        this.pacoteMulti = new Message(null, "");
        this.pacoteUni = new Message(null, "");
        this.pacoteAny = new Message(null, "");
    }


    public Message criaMulticast(String remetente, String conteudo, String hora, Map<String,Address> listaDeContatos) {//multicast
        Mensagem msg = new Mensagem(conteudo, remetente, hora);
        Pacote pacote = new Pacote(msg, listaDeContatos, Tag.MENSAGEM_MULTCAST);
        this.pacoteMulti.setDest(null);//multicast
        this.pacoteMulti.setObject(pacote);
        setMessagem(conteudo);
        return this.pacoteMulti;
    }

    public Message criaUnicast(String remetente, Address destinatario, String conteudo) {//unicast
        this.pacoteUni.setObject(conteudo);//unicast
        this.pacoteUni.setDest(destinatario);
        setMessagem(conteudo);
        return this.pacoteUni;
    }


    public Message criaAnycast(Address[] listaGrupo, String novoConteudo) {//anycast
        //this.pacoteAny = new Message(listaGrupo, novoConteudo);
        return pacoteAny;
    }

    public String getMessagem() {
        return this.conteudo;
        //return (String) super.getObject();
    }

    public void setMessagem(String msgChat) {
        this.conteudo = msgChat;
    }
}
