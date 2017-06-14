package br.edu.ifmg.samuelterra;


import org.jgroups.Address;
import org.jgroups.Message;


public class MensagemChat {

    private String conteudo;
    private Message pacoteMulti, pacoteUni;
    //private AnycastMessage pacoteAny;

    public MensagemChat() {
        this.pacoteMulti = new Message(null, "");
        this.pacoteUni = new Message(null, "");
        //this.pacoteAny = new AnycastMessage(null, "");
    }


    public Message criaMulticast(String novoConteudo) {//unicast
        //this.pacoteMulti.setDest(null);//multicast
        this.pacoteMulti.setObject(novoConteudo);
        return this.pacoteMulti;
    }

    public Message criaUnicast(Address destinatarioJGroups, String novoConteudo) {//multicast
        this.pacoteUni.setObject(this.conteudo);//unicast
        this.pacoteUni.setDest(destinatarioJGroups);
        return this.pacoteUni;
    }


    /*public AnycastMessage criaAnycast(Address[] listaGrupo, String novoConteudo) {//anycast
        this.pacoteAny = new AnycastMessage(listaGrupo, novoConteudo);
        return pacoteAny;
    }

    public String getMessagem() {
        return this.conteudo;
        //return (String) super.getObject();
    }

    public void setMessagem(String msgChat) {
        this.conteudo = msgChat;
        //this.setObject(msgChat);
    }*/
}
