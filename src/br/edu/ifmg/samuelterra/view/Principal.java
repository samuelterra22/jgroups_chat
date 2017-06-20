package br.edu.ifmg.samuelterra.view;

import br.edu.ifmg.samuelterra.controller.CriaMensagem;
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

import javax.swing.text.StyledEditorKit;
import java.io.*;
import java.util.*;

public class Principal extends ReceiverAdapter {

    private String nickname = null;
    private JChannel canal = null;

    private String chatName;

    private MessageDispatcher despachante;

    final Map<String, Address> listaDeContatos = new HashMap<String, Address>();
    final List<String> state = new LinkedList<String>();

    /* variáveis da classe */
    private String nomeCanal;
    private Address meuEndereco;

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

    public void mudarApelido(){
        if (nickname==null){
            System.out.println("Apelido ainda não definido!");
            System.out.println("Informe qual nickname deseja usar:");
            String teclado = leTextoTeclado();
            if (teclado!=null){
                this.nickname = teclado;
            }
        }else {
            System.out.println("Apelido atual: "+ nickname);
            System.out.println("Informe qual nickname deseja usar:");
            String teclado = leTextoTeclado();
            if (teclado!=null){
                this.nickname = teclado;
            }
        }
    }

    public String getNickname(){
        if (nickDefinido())
            return nickname;
        else
            return "Não definido.";
    }

    public Boolean nickDefinido(){
        return nickname != null;
    }

    public Boolean isOnline(){
        return canal != null;

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
        listaDeContatos.remove(nickname);
        canal.disconnect();
        canal.close();
        canal = null;
    }

    public Boolean nickExiste(){
        return listaDeContatos.containsKey(nickname);
    }

    public void conecta() throws Exception {

        System.out.println(canal);
        System.out.println(isOnline());

        if (!isOnline()){
            if (nickDefinido()){
                if (!nickExiste()){
                    System.out.println("Você não está online, conectando...");
                    canal = new JChannel();
                    canal.setReceiver(this);
                    canal.connect("JChat");
                    this.meuEndereco = canal.getAddress();
                    canal.getState(null, 10000);
                    listaDeContatos.put(nickname, meuEndereco);
                }else {
                    System.out.println("O apelido definido escolhido já está em uso. Escolha outro e tente novamente.");
                }
            }else {
                System.out.println("Você ainda não definil um apelido. Defina um e tente novamente.");
            }
        }else{
            System.out.println("Você está online, deseja realmente desconectar? ");
            Boolean op = leBoolTeclado();
            if ((op!=null)&&(op)){
                System.out.println("Desconectando...");
                ficarOffline();
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

        if (p.getTag() == Tag.ATUALIZA_CONTATOS) {
            listaDeContatos.putAll(p.getListaDeContatos());
        } else {
            Mensagem msg = p.getMensagem();

            System.out.println("["+msg.getHora()+"]" + msg.getRemetente() + ": " + msg.getMensagem());

            listaDeContatos.put(msg.getRemetente(), pacote.getSrc());

            listaDeContatos.putAll(p.getListaDeContatos());

            synchronized (state) {
                state.add(msg.getMensagem());
            }
        }

        //System.out.println(getUsuariosOnline().size());
        //System.out.println(listaDeContatos);
    }

    public void viewAccepted(View v) {
        /* printa na tela informado ao grupo que um novo usuário está na conversa */
        altualizaListaDeContatos();
    }

    public void altualizaListaDeContatos() {
        Pacote pacote = new Pacote(null, listaDeContatos, Tag.ATUALIZA_CONTATOS);
        Message message = new Message(null, pacote);
        try {
            canal.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RspList sendMultCast(String conteudo) throws Exception {

        Address cluster = null; //endereço null significa TODOS os membros do cluster
        Message mensagem = new Message(cluster, "{MULTICAST} " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        return despachante.castMessage(null, mensagem, opcoes);
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

    public void verAmigosOnline(){
        if (getNumeroDeUsuariosOnline() == 1){
            System.out.println("Apenas você online.");
        }else {
            for (Usuario u : getUsuariosOnline()) {
                System.out.println(u.getNickname());
            }
        }
    }

    public void enviaMensagemAmigo(){
        if (isOnline() && nickDefinido()){
            System.out.println("Conversa privada.");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
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

                    Message msg = new CriaMensagem().criaMulticast(nickname, line, getTime(), listaDeContatos);

                    canal.send(msg);

                } catch (Exception e) {
                    System.out.println("Erro: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }else {
            System.out.println("Vocẽ deve ficar online e definir um nick primeiro!");
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
        menuPrincipal.append("5. Gerenciar grupos\n");
        menuPrincipal.append("6. Sobre\n");
        menuPrincipal.append("7. Sair\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int opcao = 0;
        boolean sair = false;

        while (!sair){
            System.out.println(menuPrincipal);
            System.out.println("Status: "+isConectado());
            System.out.println("Apelido: "+getNickname());
            System.out.flush();
            opcao = Integer.parseInt(in.readLine());
            switch (opcao){
                case (1):{
                    //System.out.println("Ficar on-line");
                    conecta();
                    break;
                }
                case (2):{
                    mudarApelido();
                    break;
                }
                case (3):{
                    //System.out.println("Ver lista de contatos");
                    verAmigosOnline();
                    break;
                }
                case (4):{
                    //System.out.println("Enviar mensagem para um amigo");
                    enviaMensagemAmigo();
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
