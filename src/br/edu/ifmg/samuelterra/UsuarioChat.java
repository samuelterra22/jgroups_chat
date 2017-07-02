package br.edu.ifmg.samuelterra;


import br.edu.ifmg.samuelterra.controller.Usuario;
import br.edu.ifmg.samuelterra.model.Mensagem;
import br.edu.ifmg.samuelterra.model.Pacote;
import br.edu.ifmg.samuelterra.model.Tag;
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

    final Map<String, Address> listaDeContatos = new HashMap<String, Address>();
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

        listaDeContatos.put(nickname, canal.getAddress());

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

                //Message msg = new CriaMensagem().criaMulticast(/*nickname*/null, line, getTime(), listaDeContatos, lis);

                //canal.send(msg);

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

        Pacote p = (Pacote) pacote.getObject();

        if (p.getTag() == Tag.ATUALIZA_DADOS) {
            listaDeContatos.putAll(p.getListaDeContatos());
        } else {
            Mensagem msg = p.getMensagem();

            /* printa na tela a mensagem enviada no chat */
            System.out.println("Usuário " + msg.getRemetente() + " enviou a mensagem: " + msg.getMensagem());

            listaDeContatos.put(msg.getRemetente().getNickname(), pacote.getSrc());

            listaDeContatos.putAll(p.getListaDeContatos());

            synchronized (state) {
                state.add(msg.getMensagem());
            }
        }

        //System.out.println("Quantidade de usuários online: "+getNumeroDeUsuariosOnline());
        //getListaDeNicknames();
        System.out.println(getUsuariosOnline().size());
    }

    public void viewAccepted(View v) {
        /* printa na tela informado ao grupo que um novo usuário está na conversa */
        altualizaListaDeContatos();
    }

    public void altualizaListaDeContatos() {
        //Tag.MENSAGEM_MULTCAST
       // Pacote pacote = new Pacote(null, listaDeContatos, lis Tag.ATUALIZA_DADOS);
        //Message message = new Message(null, pacote);
        try {
            //canal.send(message);
         //   sendMultCast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RspList sendMultCast(Message message) throws Exception {
        /*
        Address cluster = null; //endereço null significa TODOS os membros do cluster
        Message mensagem = new Message(cluster, "{MULTICAST} " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        return despachante.castMessage(null, mensagem, opcoes);
        */

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        return despachante.castMessage(null, message, opcoes);

    }

    private String sendUniCast(Address destino, String conteudo) throws Exception {

        Message mensagem = new Message(destino, "{ UNICAST } " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_FIRST); // não espera receber a resposta do destino (ALL, MAJORITY, FIRST, NONE)


        return despachante.sendMessage(mensagem, opcoes);
    }

    private RspList sendAnyCast(List<Address> grupo, String conteudo) throws Exception {

        //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados
        Message mensagem = new Message(null, "{ ANYCAST } " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_MAJORITY); // espera receber a resposta da maioria do grupo (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(true);

        return despachante.castMessage(grupo, mensagem, opcoes);
    }

    private String getTime() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" +
                Calendar.getInstance().get(Calendar.MINUTE) + ":" +
                Calendar.getInstance().get(Calendar.SECOND);
    }

    public int getNumeroDeUsuariosOnline(){
        return listaDeContatos.size();
    }

    public List<String> getListaDeNicknames(){
        List<String> nicks = new ArrayList<>();
        nicks.addAll(listaDeContatos.keySet());
        return nicks;
    }

    public List<Usuario> getUsuariosOnline(){
        List<Usuario> usuarios = new ArrayList<>();
        List<String> nicks = getListaDeNicknames();
        for (String nick : nicks) {
            usuarios.add(new Usuario(nick, listaDeContatos.get(nick)));
        }
        return usuarios;
    }

}
