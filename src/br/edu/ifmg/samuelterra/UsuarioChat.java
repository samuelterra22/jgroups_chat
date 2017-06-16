package br.edu.ifmg.samuelterra;


import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import java.io.*;
import java.util.*;

public class UsuarioChat extends ReceiverAdapter {

    private String chatName;

    private MessageDispatcher despachante;

    final Map<String, Address> nicknameToAddress = new HashMap<String, Address>();
    final List<String> state = new LinkedList<String>();

    /* variáveis da classe */
    private JChannel canal;
    private String nomeCanal;
    private String nickname;
    private Address meuEndereco;

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

        /* guarda o meu endereço caso precise no envio de mensagens */
        this.meuEndereco = canal.getAddress();

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

                Message msg = new CriaMensagemChat().criaMulticast(nickname, line, getTime());

                canal.send(msg);

            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        HashMap<String, Address> us = (HashMap<String, Address>) Util.objectFromStream(new DataInputStream(input));
        /*
         * Digamos, por exemplo, que duas Threads diferentes tentem chamar o método add para um dado objeto.
         * Como é o método é synchronized, uma Thread terá de esperar que a Thread que chamou o método primeiro termine
         * a execução do mesmo. A finalidade do synchronized é evitar que você tenha problemas com estados
         * indeterminados em um programa. Suponha que você tivesse um método para remover elementos do ArrayList boxes.
         * Se esse método não fosse synchronized, poderia ocorrer de você ter duas Threads tentando remover o mesmo
         * objeto do ArrayList, o que provocaria problemas inesperados
         *
         * */
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }

        System.out.println(list.size() + " mensagens no histórico do chat:");
        for (String str : list) {
            System.out.println(str);
        }
    }

    public void receive(Message pacote) {

        Mensagem msg = (Mensagem) pacote.getObject();

        /* printa na tela a mensagem enviada no chat */
        System.out.println("Usuário "+msg.getRemetente()+" enviou a mensagem: "+msg.getMensagem());


        synchronized (state) {
            state.add((String) msg.getMensagem());
        }

        //System.out.println(nicknameToAddress.size());
    }

    public void viewAccepted(View v) {
        /* printa na tela informado ao grupo que um novo usuário está na conversa */
        System.out.println("Usuários on-line " + v.getMembers());
    }

    private String getTime() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" +
                Calendar.getInstance().get(Calendar.MINUTE) + ":" +
                Calendar.getInstance().get(Calendar.SECOND);
    }

    public RspList sendMultCast(String conteudo) throws Exception{

        Address cluster = null; //endereço null significa TODOS os membros do cluster
        Message mensagem=new Message(cluster, "{MULTICAST} "+conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        return despachante.castMessage(null, mensagem, opcoes);
    }

    private String sendUniCast(Address destino, String conteudo) throws Exception{

        Message mensagem = new Message(destino, "{ UNICAST } " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_FIRST); // não espera receber a resposta do destino (ALL, MAJORITY, FIRST, NONE)



        return despachante.sendMessage(mensagem, opcoes);
    }

    private RspList sendAnyCast(List<Address> grupo, String conteudo) throws Exception{

        Message mensagem=new Message(null, "{ ANYCAST } " + conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_MAJORITY); // espera receber a resposta da maioria do grupo (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(true);

        return despachante.castMessage(grupo, mensagem, opcoes);
    }


    private List<Address> getMembersOfCluster() {
        return canal.getView().getMembers();
    }

    private Address getLastMemberOfCluster() {
        return canal.getView().getMembers().get(getNumOfMenbers() - 1);
    }

    private int getNumOfMenbers() {
        return canal.getView().getMembers().size();
    }

    private void addUserToListNicks() {
        nicknameToAddress.put(this.nickname, getLastMemberOfCluster());
    }

}
