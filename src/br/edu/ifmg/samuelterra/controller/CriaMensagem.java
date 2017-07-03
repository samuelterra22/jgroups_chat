package br.edu.ifmg.samuelterra.controller;

import br.edu.ifmg.samuelterra.model.*;
import org.jgroups.Address;
import org.jgroups.Message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/***********************************************************************************************************************
 *                                         JGroups Chat v1.0                                                           *
 *   Alunos:        Matheus Calixto | Samuel Terra                                                                     *
 *   Professor:     Everthon Valadão                                                                                   *
 *   Disciplina:    Sistemas Distribuidos                                                                              *
 *   Modificado em: 03/07/2017                                                                                         *
 *                                                                                                                     *
 **********************************************************************************************************************/
public class CriaMensagem implements Serializable {

    private String conteudo;
    private Message pacoteUni, pacoteAny;

    public CriaMensagem() {
        this.pacoteUni = new Message(null, "");
        this.pacoteAny = new Message(null, "");
    }

    /**
     * Retorna um objeto Message ja montado com o Pacote contendo as informações necessarias
     * */
    public Message criaUnicast(Usuario destinatario, Usuario remetente,  String conteudo, String hora,
                               Map<String, Address> listaDeContatos, Map<Address, String> listaDeAddress,
                               Map<String, Grupo> listaDeGrupos, Map<String, List<String>> conversas) {//unicast

        Mensagem msg = new Mensagem(destinatario, remetente, conteudo, hora);
        Pacote pacote = new Pacote(msg, listaDeContatos, listaDeAddress,listaDeGrupos, conversas, Tag.MENSAGEM_UNICAST, null);

        this.pacoteUni.setSrc(remetente.getAddress());
        this.pacoteUni.setDest(destinatario.getAddress());
        this.pacoteUni.setObject(pacote);//unicast
        setMessagem(conteudo);
        return this.pacoteUni;
    }


    /**
     * Retorna o conteudo da mensagem
     * */
    public String getMessagem() {
        return this.conteudo;
        //return (String) super.getObject();
    }

    public void setMessagem(String msgChat) {
        this.conteudo = msgChat;
    }
}
