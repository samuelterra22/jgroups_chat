package br.edu.ifmg.samuelterra.view;

import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Principal extends ReceiverAdapter {

    private String nickname = null;
    private JChannel canal = null;

    public Principal() {    }

    private Boolean leBoolTeclado(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.flush();
        try {
             String entrada = in.readLine();
             return entrada.toLowerCase().contains("s") || entrada.toLowerCase().contains("sim");

        } catch (IOException e) {
            return null;
        }
    }

    private String leTextoTeclado(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.flush();
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void mudaNickName(){
        if (nickname==null){
            System.out.println("Nickname ainda não definido!");
            System.out.println("Informe qual nickname deseja usar:");
            String teclado = leTextoTeclado();
            if (teclado!=null){
                this.nickname = teclado;
            }
        }else {
            System.out.println("Nickname atual: "+ nickname);
            System.out.println("Informe qual nickname deseja usar:");
            String teclado = leTextoTeclado();
            if (teclado!=null){
                this.nickname = teclado;
            }
        }
    }

    public String isConectado(){
        if (canal == null){
            return "Offline.";
        }
        else{
            return "Online.";
        }
    }

    public void ficarOffline(){
        canal.disconnect();
        canal.close();
        canal = null;
    }

    public void conecta() throws Exception {

        if (canal == null){
            System.out.println("Você não está online, conectando...");
            canal = new JChannel();
            canal.setReceiver(this);
            canal.connect("JChat");
        }else{
            System.out.println("Você está online, deseja realmente desconectar? ");
            Boolean op = leBoolTeclado();
            if ((op!=null)&&(op==true)){
                System.out.println("Desconectando...");
                ficarOffline();
            }
        }
    }

    public void menuPrincipal() throws Exception {

        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("***  JGroups Chat v1.0  ***\n");
        menuPrincipal.append("Selecione uma opção:\n\n");
        menuPrincipal.append("1. Ficar online/offline\n");
        menuPrincipal.append("2. Mudar nickname\n");
        menuPrincipal.append("3. Ver lista de contatos\n");
        menuPrincipal.append("4. Enviar mensagem para um amigo\n");
        menuPrincipal.append("5. Grupos\n");
        menuPrincipal.append("6. Sobre\n");
        menuPrincipal.append("7. Sair\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int opcao = 0;
        boolean sair = false;

        while (!sair){
            System.out.println(menuPrincipal);
            System.out.println("Status: "+isConectado());
            System.out.flush();
            opcao = Integer.parseInt(in.readLine());
            switch (opcao){
                case (1):{
                    //System.out.println("Ficar on-line");
                    conecta();
                    break;
                }
                case (2):{
                    //System.out.println("Mudar nickname");
                    mudaNickName();
                    break;
                }
                case (3):{
                    System.out.println("Ver lista de contatos");
                    break;
                }
                case (4):{
                    System.out.println("Enviar mensagem para um amigo");
                    break;
                }
                case (5):{
                    System.out.println("Grupos");
                    break;
                }
                case (6):{
                    System.out.println("Sobre");
                    break;
                }
                case (7):{
                    System.out.println("Bye bye...");
                    sair = true;
                    ficarOffline();
                    break;
                }
                default:{
                    System.out.println("Opção inválida!");
                    break;
                }
            }
        }

    }

}
