package br.edu.ifmg.samuelterra;


import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.*;
import java.util.*;

public class UsuarioChat extends ReceiverAdapter {

    private String chatName;
    private Map<String, Address> usuarios = new HashMap<String,Address>();
    final List<String> state = new LinkedList<String>();

    /* variáveis da classe */
    private JChannel canal;
    private String nomeCanal;
    private String nickname;

    public UsuarioChat(String nomeCanal, String nickname) {
        /* define o nome do canal e o apelido do usuário do chat */
        this.nomeCanal = nomeCanal;
        this.nickname = nickname;
    }

    public void start() throws Exception {
        /* instancia um novo canal */
        canal = new JChannel();
        /* seta qual classe vai receber as mensagens */
        canal.setReceiver(this);
        /* realiza a conecxão do canal de acordo com o nome */
        canal.connect(nomeCanal);

        /* adiciona usuario no hash que faz a ligacao de nick <-> address*/
        //addUserToListNicks();


        canal.getState(null, 10000);

        /* chama o método que realiza a leitura e envio das mensagens */
        eventLoop();
        /* ao terminar de executar, fecha o canal */
        canal.close();
    }

    private void eventLoop() {
        /* instancia um leitor para que seja possivel capturar irnfomações do teclado */
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        /* fica em loop ate que receba a tag para fechar o chat */
        while (true) {
            try {

                System.out.print("> ");
                System.out.flush();
                line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit")) {
                    System.out.println("Bye");
                    break;
                }
                if (line.startsWith("new group"))
                    System.out.println("Adicionar novo grupo");

                if (line.startsWith("add user"))
                    System.out.println("Adicionar novo usuario");

                line = "["+getTime()+"] [" + nickname + "]: " + line;
                Message msg = new MensagemChat().criaMulticast(line);
                canal.send(msg);

            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized(state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        List<String> list=(List<String>)Util.objectFromStream(new DataInputStream(input));
        /*
         * Digamos, por exemplo, que duas Threads diferentes tentem chamar o método add para um dado objeto.
         * Como é o método é synchronized, uma Thread terá de esperar que a Thread que chamou o método primeiro termine
         * a execução do mesmo. A finalidade do synchronized é evitar que você tenha problemas com estados
         * indeterminados em um programa. Suponha que você tivesse um método para remover elementos do ArrayList boxes.
         * Se esse método não fosse synchronized, poderia ocorrer de você ter duas Threads tentando remover o mesmo
         * objeto do ArrayList, o que provocaria problemas inesperados
         *
         * */
        synchronized(state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println(list.size() + " mensagens no histórico do chat:");
        for(String str: list) {
            System.out.println(str);
        }
    }

    public void receive(Message pacote) {
        /* printa na tela a mensagem enviada no chat */
        System.out.println(pacote.getObject());

        synchronized(state) {
            state.add((String) pacote.getObject());
        }

        System.out.println(usuarios.toString());
    }

    public void viewAccepted(View v) {
        /* printa na tela informado ao grupo que um novo usuário está na conversa */
        System.out.println("Usuários on-line " + v.getMembers());
    }

    private String getTime(){
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+
                Calendar.getInstance().get(Calendar.MINUTE)+":"+
                Calendar.getInstance().get(Calendar.SECOND);
    }

    private List<Address> getMembersOfCluster() {
        return canal.getView().getMembers();
    }

    private Address getLastMemberOfCluster() {
        return canal.getView().getMembers().get(getNumOfMenbers()-1);
    }

    private int getNumOfMenbers(){
        return canal.getView().getMembers().size();
    }

    private void addUserToListNicks(){
        usuarios.put(this.nickname, getLastMemberOfCluster());
    }

}
