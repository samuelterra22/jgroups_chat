package br.edu.ifmg.samuelterra;


import org.jgroups.Address;
import org.jgroups.Message;


public class CriaMensagemChat {

    private String conteudo;
    private Message pacoteMulti, pacoteUni, pacoteAny;

    public CriaMensagemChat() {
        this.pacoteMulti = new Message(null, "");
        this.pacoteUni = new Message(null, "");
        this.pacoteAny = new Message(null, "");
    }


    public Message criaMulticast(String remetente, String conteudo, String hora) {//unicast
        Mensagem msg = new Mensagem(remetente, conteudo, hora, Tag.MENSAGEM_MULTCAST);
        this.pacoteMulti.setDest(null);//multicast
        this.pacoteMulti.setObject(msg);
        setMessagem(conteudo);
        return this.pacoteMulti;
    }

    public Message criaUnicast(String remetente, Address destinatarioJGroups, String conteudo) {//multicast
        this.pacoteUni.setObject(conteudo);//unicast
        this.pacoteUni.setDest(destinatarioJGroups);
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
