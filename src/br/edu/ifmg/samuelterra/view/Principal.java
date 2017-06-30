package br.edu.ifmg.samuelterra.view;

import br.edu.ifmg.samuelterra.controller.CriaMensagem;
import br.edu.ifmg.samuelterra.controller.Usuario;
import br.edu.ifmg.samuelterra.model.Grupo;
import br.edu.ifmg.samuelterra.model.Mensagem;
import br.edu.ifmg.samuelterra.model.Pacote;
import br.edu.ifmg.samuelterra.model.Tag;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import java.io.*;
import java.security.Guard;
import java.util.*;

public class Principal extends ReceiverAdapter implements RequestHandler {

    private String nickname = null;
    private JChannel canal = null;

    private Usuario eu;

    private boolean conversando = false;

    private String chatName;

    private MessageDispatcher despachante;

    final Map<String, Address> listaDeContatos = new HashMap<String, Address>();
    final Map<String, Grupo> listaDeGrupos     = new HashMap<String, Grupo>();

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

    private Integer leNumeroTeclado(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Integer n = null;
        try {

            n = Integer.parseInt(in.readLine());
        } catch (Exception e) {
            return null;
        }
        return n;
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

                    despachante = new MessageDispatcher(canal, null, null, this);
                    despachante.setRequestHandler(this);
                    despachante.setMessageListener(this);
                    despachante.setMembershipListener(this);

                    canal.setReceiver(this);
                    canal.connect("JChat");

                    this.meuEndereco = canal.getAddress();
                    this.eu = new Usuario(this.nickname, meuEndereco);
                    canal.getState(null, 10000);

                    listaDeContatos.put(nickname, meuEndereco);

                    altualizaListaDeContatos();
                }else {
                    System.out.println("O apelido definido escolhido já está em uso. Escolha outro e tente novamente.");
                }
            }else {
                System.out.println("Você ainda não definil um apelido. Defina um e tente novamente.");
            }
        }else{
            System.out.println("Você está online, deseja realmente desconectar? (s/n)");
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

    @Override
    public Object handle(Message message) throws Exception {

        Pacote p = (Pacote) message.getObject();

        Mensagem msgChat = p.getMensagem();

        System.out.println("DEBUG Handle: Tag: "+p.getTag());

        /*if(p.getTag() == Tag.MENSAGEM_UNICAST){
            if (!conversando){
                Usuario amigo = msgChat.getDestinatario();
                enviaMensagemAmigo(amigo);
                conversando = true;
            }else{
                System.out.println("["+msgChat.getHora()+"]" + msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());
            }
        }else {
            System.out.println("["+msgChat.getHora()+"]" + msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());
        }*/


        System.out.println("["+msgChat.getHora()+"]" + msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());


        listaDeContatos.put(msgChat.getRemetente().getNickname(), message.getSrc());

        listaDeContatos.putAll(p.getListaDeContatos());
        listaDeGrupos.putAll(p.getListDeGrupos());

        synchronized (state) {
            state.add(msgChat.getMensagem());
        }

            return null;
    }

    public void receive(Message msgJGroups) {

        Pacote p = (Pacote) msgJGroups.getObject();

        System.out.println("DEBUG Receive: Tag: "+p.getTag());

        if (p.getTag() == Tag.ATUALIZA_CONTATOS) {
            listaDeContatos.putAll(p.getListaDeContatos());
            listaDeGrupos.putAll(p.getListDeGrupos());
        }
        /*else if(p.getTag() == Tag.MENSAGEM_UNICAST){
            if (!conversando){
                Mensagem msgChat = p.getMensagem();
                Usuario amigo = msgChat.getDestinatario();
                enviaMensagemAmigo(amigo);
                conversando = true;
            }else{
                Mensagem msgChat = p.getMensagem();
                System.out.println("["+msgChat.getHora()+"]" + msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());
            }
        }*/
        else {
            Mensagem msgChat = p.getMensagem();

            System.out.println("["+msgChat.getHora()+"]" + msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());

            listaDeContatos.put(msgChat.getRemetente().getNickname(), msgJGroups.getSrc());

            listaDeContatos.putAll(p.getListaDeContatos());
            listaDeGrupos.putAll(p.getListDeGrupos());

            synchronized (state) {
                state.add(msgChat.getMensagem());
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

        Pacote pacote = new Pacote(null, listaDeContatos, listaDeGrupos, Tag.ATUALIZA_CONTATOS);
        Message message = new Message(null, pacote);
        try {
            System.out.println("Atualizando lista de contatos...");
            canal.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*Pacote pacote = new Pacote(null, listaDeContatos, Tag.ATUALIZA_CONTATOS);
        Message message = new Message(null, pacote);

        System.out.println("Atualizando lista de contatos...");
        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_FIRST); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);
        try {
            despachante.castMessage(null, message, opcoes); //MULTICAST
        } catch (Exception e) {
            e.printStackTrace();
        }*/



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
        if (isOnline()){
            if (getNumeroDeUsuariosOnline() == 1){
                System.out.println("Apenas você online.");
            }else {
                System.out.println(getNumeroDeUsuariosOnline() +" usuários online (incluindo você).");
                for (Usuario u : getUsuariosOnline()) {
                    if (u.getAddress().equals(eu.getAddress())){
                        System.out.println(u.getNickname()+" (Você)");
                    }else {
                        System.out.println(u.getNickname());
                    }
                }
            }
        }else{
            System.out.println("Você precisa estar online para ver a lista de amigos");
        }
    }

    public Usuario escolheAmigo(){

        List<String> listaDeNicks = new ArrayList<>();
        listaDeNicks.addAll(listaDeContatos.keySet());
        Usuario amigo = null;
        String op;

        while (amigo == null){
            System.out.println("Selecione um amigo para iniciar a conversa:");
            for (int i=0; i < listaDeNicks.size(); i++) {
                if (listaDeContatos.get(listaDeNicks.get(i)).equals(eu.getAddress())){
                    System.out.println("("+i+") "+ listaDeNicks.get(i)+" (Você)");
                }else {
                    System.out.println("("+i+") "+ listaDeNicks.get(i));
                }
            }
            op = leTextoTeclado();
            if (op != null){
                if ((Integer.parseInt(op) >= 0)&&(Integer.parseInt(op) < listaDeNicks.size())){
                    amigo = new Usuario(listaDeNicks.get(Integer.parseInt(op)),
                            listaDeContatos.get(listaDeNicks.get(Integer.parseInt(op))));
                }else {
                    System.out.println("Opção inválida");
                }
            }
        }
        System.out.println("Retornando: "+amigo.getNickname());
        return amigo;
    }

    public void enviaMensagemGrupo(){
        if (getNumeroDeUsuariosOnline() > 1){
            if (isOnline() && nickDefinido()){
                System.out.println("Conversa em grupo.");

                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                System.out.print("Use 'quit' ou 'exit' para sair da conversa.");
                while (true) {
                    try {
                        System.out.print("> ");
                        System.out.flush();
                        line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit")) {
                            break;
                        }

                        //Message msg = new CriaMensagem().criaUnicast(amigo, eu, line, getTime(), listaDeContatos);
                        //Message msg = new CriaMensagem().criaMulticast(eu, "teste",getTime(), listaDeContatos);
                        //canal.send(msg);

                        System.out.println("["+getTime()+"]" + eu.getNickname() + ": " +line);
                        //enviaMulticast(line);

                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }else {
                System.out.println("Vocẽ deve ficar online e definir um nick primeiro!");
            }
        }else {
            System.out.println("Para inicial uma conversa é necessário ter pelo menos dois usuários online.");
        }
    }

    public void enviaMensagemAmigo(Usuario amigo){
        if (getNumeroDeUsuariosOnline() > 1){
            if (isOnline() && nickDefinido()){
                System.out.println("Conversa privada.");

                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                System.out.println("Use 'quit' ou 'exit' para sair da conversa.");
                while (true) {
                    try {
                        System.out.print("> ");
                        System.out.flush();
                        line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit")) {
                            break;
                        }

                        System.out.println("["+getTime()+"]" + eu.getNickname() + ": " +line);
                        enviaUnicast(amigo, eu, line);


                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }else {
                System.out.println("Vocẽ deve ficar online e definir um nick primeiro!");
            }
        }else {
            System.out.println("Para inicial uma conversa é necessário ter pelo menos dois usuários online.");
        }
    }

    public void enviaMensagemAmigo(){
        if (getNumeroDeUsuariosOnline() > 1){
            if (isOnline() && nickDefinido()){
                System.out.println("Conversa privada.");

                Usuario amigo = escolheAmigo();

                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                System.out.println("Use 'quit' ou 'exit' para sair da conversa.");
                while (true) {
                    try {
                        System.out.print("> ");
                        System.out.flush();
                        line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit")) {
                            break;
                        }

                        System.out.println("["+getTime()+"]" + eu.getNickname() + ": " +line);
                        enviaUnicast(amigo, eu, line);


                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }else {
                System.out.println("Vocẽ deve ficar online e definir um nick primeiro!");
            }
        }else {
            System.out.println("Para inicial uma conversa é necessário ter pelo menos dois usuários online.");
        }
    }

    public Grupo criaGrupo(){

        List<String> listaDeNicks = new ArrayList<>();
        listaDeNicks.addAll(listaDeContatos.keySet());

        Grupo grupo = new Grupo();

        List<String> listaSelecionados = new ArrayList<>();

        Integer op;

        while (true){
            System.out.println("Selecione amigos para adicionar no grupo: (Informa '-1' para continuar)");

            // printa os amigos
            for (int i=0; i < listaDeNicks.size(); i++) {
                // printa se nao tiver na lista de selecionado
                if (!listaSelecionados.contains(listaDeNicks.get(i))) {
                    if (listaDeContatos.get(listaDeNicks.get(i)).equals(eu.getAddress())) {
                        System.out.println("(" + i + ") " + listaDeNicks.get(i) + " (Você)");
                    } else {
                        System.out.println("(" + i + ") " + listaDeNicks.get(i));
                    }
                }else{
                    System.out.println("(" + i + ") " + listaDeNicks.get(i) + " (Selecionado)");
                }
            }
            op = leNumeroTeclado();
            if (op != null){
                if ((op >= 0)&&(op < listaDeNicks.size())){

                    if(listaDeContatos.get(listaDeNicks.get(op)).equals(eu.getAddress())){
                        System.out.println("Você não pode selecionar você mesmo.");
                    }else {
                        listaSelecionados.add(listaDeNicks.get(op));
                    }

                }else if(op == -1){
                    // sai do menu de escolha de amigos
                    break;
                }
                else {
                    System.out.println("Opção inválida");
                }
            }
        }

        // adiciona usuarios selecionados ao grupo
        for (String u :listaSelecionados) {
            grupo.adicionaUsuario(new Usuario(u, listaDeContatos.get(u)));
        }

        String nomeGrupo = null;

        // define o nome do grupo
        while (nomeGrupo == null){
            System.out.println("Escolha um nome para o grupo:");
            nomeGrupo = leTextoTeclado();
        }
        grupo.setNome(nomeGrupo);

        // define o coordenador como quem esta criando o grupo
        grupo.setCoordenador(eu);

        // adiciona o coordenador no grupo tbm
        grupo.adicionaUsuario(eu);

        // adiciona o grupo na lista de grupos
        this.listaDeGrupos.put(eu.getNickname(), grupo);

        System.out.println("Grupo '"+nomeGrupo+"' criado com sucesso!");

        altualizaListaDeContatos();

        // retorna o grupo
        return grupo;
    }

    public void verGrupos(){

        System.out.println("Foram encontrados "+listaDeGrupos.size()+" grupo(s)");

        List<Grupo> grupos = new ArrayList<>(listaDeGrupos.values());

        for (Grupo grupo : grupos) {
            System.out.print("Nome: "+grupo.getNome() + " Coord. -> " +grupo.getCoordenador().getNickname());
            if (pertenceAoGrupo(eu, grupo))
                System.out.println(" (Você pertence a este grupo)");
            else
                System.out.println();
        }
    }

    public boolean pertenceAoGrupo(Usuario usuario, Grupo grupo){

        List<Usuario> list = grupo.getUsuarios();

        for (Usuario u : list) {
            if (Objects.equals(u.getNickname(), usuario.getNickname()))
                return true;
        }

        return false;
    }

    public void detalhesGrupo(){

        List<Grupo> grupos = new ArrayList<>(listaDeGrupos.values());
        Integer op;

        while (true){
            System.out.println("Selecione um grupo para ver detalhes: (Informa '-1' para sair)");

            // printa os amigos
            for (int i=0; i < grupos.size(); i++) {
                System.out.println("("+i+") "+grupos.get(i).getNome());
            }
            op = leNumeroTeclado();
            if (op != null){
                if ((op >= 0)&&(op < grupos.size())){

                    System.out.println("=======================================================");
                    System.out.println("Nome:               "+grupos.get(op).getNome());
                    System.out.println("Coordenador:        "+grupos.get(op).getCoordenador().getNickname());
                    System.out.println("Numero de amigos:   "+grupos.get(op).getUsuarios().size());
                    System.out.println("=======================================================");

                    for (Usuario u : grupos.get(op).getUsuarios()) {
                        System.out.println(u.getNickname() +" -> "+ u.getAddress());
                    }

                    System.out.println("=======================================================");

                }else if(op == -1){
                    // sai do menu de escolha de amigos
                    break;
                }
                else {
                    System.out.println("Opção inválida");
                }
            }
        }
    }

    public void menuPrincipal() throws Exception {

        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("\n***  JGroups Chat v1.0  ***\n");
        menuPrincipal.append("\n***    Menu Principal  ***\n");
        menuPrincipal.append("Selecione uma opção:\n\n");
        menuPrincipal.append("1. Ficar online/offline\n");
        menuPrincipal.append("2. Mudar nickname\n");
        menuPrincipal.append("3. Ver lista de amigos\n");
        menuPrincipal.append("4. Enviar mensagem para um amigo\n");
        menuPrincipal.append("5. Gerenciar grupos\n");
        menuPrincipal.append("6. Sobre\n");
        menuPrincipal.append("7. Sair\n\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Integer opcao = 0;
        boolean sair = false;

        while (!sair){
            System.out.println(menuPrincipal);
            System.out.println("Status: "+isConectado());
            System.out.println("Apelido: "+getNickname());
            System.out.flush();

            opcao = leNumeroTeclado();

            if (opcao != null){
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
                        //escolheAmigo();
                        break;
                    }
                    case (5):{
                        menuGrupos();
                        //enviaMensagemGrupo();
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

    public void menuGrupos() throws Exception {
        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("\n***  JGroups Chat v1.0  ***\n");
        menuPrincipal.append("\n***   Gerenciar Grupos  ***\n");
        menuPrincipal.append("Selecione uma opção:\n\n");
        menuPrincipal.append("1. Criar grupo\n");
        menuPrincipal.append("2. Ver grupos\n");
        menuPrincipal.append("3. Apagar grupo\n");
        menuPrincipal.append("4. Renomear grupo\n");
        menuPrincipal.append("5. Adicionar amigo ao grupo\n");
        menuPrincipal.append("6. Detalhes de um grupo\n");
        menuPrincipal.append("7. Voltar\n\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int opcao = 0;
        boolean sair = false;

        while (!sair) {
            System.out.println(menuPrincipal);
            System.out.println("Status: " + isConectado());
            System.out.println("Apelido: " + getNickname());
            System.out.flush();
            opcao = Integer.parseInt(in.readLine());
            switch (opcao) {
                case (1): {
                    //testaAnyCast();
                    criaGrupo();
                    break;
                }
                case (2): {
                    //System.out.println("");
                    verGrupos();
                    break;
                }
                case (3): {
                    //System.out.println("");
                    break;
                }
                case (4): {
                    //System.out.println("");
                    break;
                }
                case (5): {
                    //System.out.println("");
                    break;
                }
                case (6): {
                    detalhesGrupo();
                    break;
                }
                case (7): {
                    System.out.println("Voltando ao menu principal...");
                    sair = true;
                    break;
                }
                default: {
                    System.out.println("Opção inválida!");
                    break;
                }
            }
        }

    }

    private void enviaUnicast(Usuario destinatario, Usuario remetente, String conteudo) throws Exception{

        Message msg = new CriaMensagem().criaUnicast(destinatario, remetente, conteudo, getTime(), listaDeContatos, listaDeGrupos);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.Flag.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // não espera receber a resposta do destino (ALL, MAJORITY, FIRST, NONE)

        despachante.sendMessage(msg, opcoes); //UNICAST
    }

    private void enviaMulticast(String conteudo) throws Exception{

        Message mensagem=new Message(null, "{MULTICAST} "+conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //MULTICAST
        System.out.println("==> Respostas do cluster ao MULTICAST:\n" +respList+"\n");
    }


    private void enviaAnycast(Grupo g, String conteudo) throws Exception{

        Collection<Address> grupo = g.getEnderecos();

        Pacote p = new Pacote(new Mensagem(null, eu, "teste", getTime()), listaDeContatos, listaDeGrupos, Tag.MENSAGEM_ANYCAST);

        //Message mensagem = new Message(null, "{ ANYCAST } " + conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados
        Message mensagem = new Message(null, p); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_NONE); // espera receber a resposta da maioria do grupo (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(true);

        RspList respList = this.despachante.castMessage(grupo, mensagem, opcoes); //ANYCAST

        System.out.println("==> Respostas do grupo ao ANYCAST:\n" +respList+"\n");

    }


    private void testaAnyCast(){


        List<Address> aa = new ArrayList<>( listaDeContatos.values());
        List<String> uu = new ArrayList<>( listaDeContatos.keySet());
        List<Usuario> usuarios = new ArrayList<>();


        for (String nick :uu) {
            usuarios.add(new Usuario(nick, listaDeContatos.get(nick)));
        }

        Grupo g = new Grupo("Meu Grupo", eu, usuarios);

        try {
            enviaAnycast(g, "teste");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}


    /*

    GET_ALL: block until responses from all members (minus the suspected ones) have been received.
    GET_NONE: wait for none. This makes the call non-blocking
    GET_FIRST: block until the first response (from anyone) has been received
    GET_MAJORITY: block until a majority of members have responded

    */