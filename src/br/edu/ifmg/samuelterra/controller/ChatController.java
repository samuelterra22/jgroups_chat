package br.edu.ifmg.samuelterra.controller;


import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;

import java.util.Calendar;
import java.util.List;

public class ChatController {

    private MessageDispatcher despachante;

    public ChatController() {    }


    private void notificaOnline(){
        //this.canal.send(new Mensagem(null));
    }

    private String getTime() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" +
                Calendar.getInstance().get(Calendar.MINUTE) + ":" +
                Calendar.getInstance().get(Calendar.SECOND);
    }

}
