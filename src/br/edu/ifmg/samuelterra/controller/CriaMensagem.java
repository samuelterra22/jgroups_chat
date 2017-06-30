package br.edu.ifmg.samuelterra.controller;

import br.edu.ifmg.samuelterra.model.Grupo;
import br.edu.ifmg.samuelterra.model.Mensagem;
import br.edu.ifmg.samuelterra.model.Pacote;
import br.edu.ifmg.samuelterra.model.Tag;
import org.jgroups.Address;
import org.jgroups.Message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class CriaMensagem implements Serializable {

    private String conteudo;
    private Message pacoteMulti, pacoteUni, pacoteAny;

    public CriaMensagem() {
        this.pacoteMulti = new Message(null, "");
        this.pacoteUni = new Message(null, "");
        this.pacoteAny = new Message(null, "");
    }


    public Message criaMulticast(Usuario remetente, String conteudo, String hora, Map<String, Address> listaDeContatos, Map<String, Grupo> listaDeGrupos) {//multicast
        Mensagem msg = new Mensagem(null, remetente, conteudo, hora);
        Pacote pacote = new Pacote(msg, listaDeContatos, listaDeGrupos, Tag.MENSAGEM_MULTCAST);

        //this.pacoteMulti.setSrc(remetente.getAddress());
        this.pacoteMulti.setDest(null);//multicast
        this.pacoteMulti.setObject(pacote);
        setMessagem(conteudo);
        return this.pacoteMulti;
    }

    // retorna um objeto Message ja montado com o Pacote contendo as informações necessarias
    public Message criaUnicast(Usuario destinatario, Usuario remetente,  String conteudo, String hora, Map<String, Address> listaDeContatos, Map<String, Grupo> listaDeGrupos) {//unicast

        Mensagem msg = new Mensagem(destinatario, remetente, conteudo, hora);
        Pacote pacote = new Pacote(msg, listaDeContatos, listaDeGrupos,Tag.MENSAGEM_UNICAST);

        this.pacoteUni.setSrc(remetente.getAddress());
        this.pacoteUni.setDest(destinatario.getAddress());
        this.pacoteUni.setObject(pacote);//unicast
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
