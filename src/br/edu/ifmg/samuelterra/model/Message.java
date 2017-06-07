package br.edu.ifmg.samuelterra.model;


/**
 * Created by samuel on 06/06/17.
 */
public class Message{

    private org.jgroups.Message message;

    public Message(String message) {
        this.message = new org.jgroups.Message(null, message);
    }

    public org.jgroups.Message getMessage() {
        return message;
    }

    public void setMessage(org.jgroups.Message message) {
        this.message = message;
    }
}
