package br.edu.ifmg.samuelterra.view;

import br.edu.ifmg.samuelterra.controller.CriaMensagem;
import br.edu.ifmg.samuelterra.model.Usuario;
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
import java.util.*;

public class Principal extends ReceiverAdapter implements RequestHandler {

    private String nickname = null;
    private JChannel canal = null;

    private Usuario eu;

    private String chatName;

    private MessageDispatcher despachante;

    final Map<String, Address> listaDeContatos = new HashMap<String, Address>();
    final Map<String, Grupo> listaDeGrupos     = new HashMap<String, Grupo>();

    final Map<String, List<String>> conversas = new HashMap<String, List<String>>();

    final List<String> historico = new LinkedList<String>();

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
        if (canal.isConnected()){
            canal.disconnect();
        }
        canal.close();
        canal = null;
        salvaHistoricoOffline();
    }

    public Boolean nickExiste(){
        return listaDeContatos.containsKey(nickname);
    }

    public void conecta() throws Exception {

        System.out.println(canal);
        System.out.println(isOnline());

        //leHistoricoOffline();

        if (!isOnline()){
            if (nickDefinido()){
                if (!nickExiste()){
                    System.out.println("Você não está online, conectando...");
                    canal = new JChannel("chat2.xml");  //achou
                    //canal = new JChannel("xmls/udp.xml"); //achou
                    //canal = new JChannel("xmls/mping.xml"); //achou
                    //canal = new JChannel("xmls/udp-largecluster.xml"); //achou
                    //canal = new JChannel("xmls/toa.xml");  //achou

                    despachante = new MessageDispatcher(canal, null, null, this);
                    despachante.setRequestHandler(this);
                    despachante.setMessageListener(this);
                    despachante.setMembershipListener(this);

                    canal.setReceiver(this);
                    canal.connect("JChat");

                    this.meuEndereco = canal.getAddress();
                    this.eu = new Usuario(this.nickname, meuEndereco);

                    // obtem o nove estado do chat
                    canal.getState(null, 10000);

                    listaDeContatos.put(nickname, meuEndereco);

                    atualizaDados();
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

    /*public void getState(OutputStream output) throws Exception {
        synchronized (conversas) {
            Util.objectToStream(conversas, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        Map<String, List<String>> hist = (HashMap<String, List<String>>) Util.objectFromStream(new DataInputStream(input));
          synchronized (conversas) {
            conversas.clear();
            conversas.putAll(hist);
        }

        System.out.println(hist.size() + " histórico do chat:");
        for (String str : hist.keySet()) {
            System.out.println(str);            //pode estar errado esse print
        }
    }*/

    @Override
    public Object handle(Message message) throws Exception {

        Pacote p = (Pacote) message.getObject();

        Mensagem msgChat = p.getMensagem();

        System.out.println("DEBUG Handle: Tag: "+p.getTag());

        // mostra a mensagem recebida
        mostraMensagemRecebida(p);


        listaDeContatos.clear();
        listaDeGrupos.clear();
        conversas.clear();


        listaDeContatos.put(msgChat.getRemetente().getNickname(), message.getSrc());

        listaDeContatos.putAll(p.getListaDeContatos());
        listaDeGrupos.putAll(p.getListDeGrupos());
        conversas.putAll(p.getConversas());

        //synchronized (conversas) {
            //state.add(msgChat.getMensagem());
            historico.add("["+msgChat.getHora()+"]"+ msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());
            conversas.put(p.getGrupo().getNome(), historico);
        //}

            return null;
    }

    public void receive(Message msgJGroups) {

        Pacote p = (Pacote) msgJGroups.getObject();

        System.out.println("DEBUG Receive: Tag: "+p.getTag());

        // limpa as hashs para serem sobre escritas com as que foram recebidas
        listaDeContatos.clear();
        listaDeGrupos.clear();
        conversas.clear();

        if (p.getTag() == Tag.ATUALIZA_DADOS) {

            listaDeContatos.putAll(p.getListaDeContatos());
            listaDeGrupos.putAll(p.getListDeGrupos());
            conversas.putAll(p.getConversas());
        }
        else if (p.getTag() == Tag.MENSAGEM_ANYCAST){
            Mensagem msgChat = p.getMensagem();

            mostraMensagemRecebida(p);

            listaDeContatos.put(msgChat.getRemetente().getNickname(), msgJGroups.getSrc());

            listaDeContatos.putAll(p.getListaDeContatos());
            listaDeGrupos.putAll(p.getListDeGrupos());
            conversas.putAll(p.getConversas());

          //  synchronized (conversas) {
                //state.add(msgChat.getMensagem());

                historico.add("["+msgChat.getHora()+"]"+ msgChat.getRemetente().getNickname() + ": " + msgChat.getMensagem());
                conversas.put(p.getGrupo().getNome(), historico);
          //  }
        }else {
            Mensagem msgChat = p.getMensagem();

            mostraMensagemRecebida(p);

            listaDeContatos.put(msgChat.getRemetente().getNickname(), msgJGroups.getSrc());

            listaDeContatos.putAll(p.getListaDeContatos());
            listaDeGrupos.putAll(p.getListDeGrupos());
            conversas.putAll(p.getConversas());
        }

        //System.out.println(getUsuariosOnline().size());
        //System.out.println(listaDeContatos);
    }

    public void viewAccepted(View novaComposicaoCluster) {
        View composicaoAntigaCluster = canal.getView();



        System.out.println("DEBUG: View modificada.");
        System.out.println("DEBUG: View atual: " + composicaoAntigaCluster);

        /* printa na tela informado ao grupo que um novo usuário está na conversa */
        atualizaDados();
    }

    public void mostraMensagemRecebida(Pacote pacote){

        Mensagem mensagem = pacote.getMensagem();

        if (pacote.getGrupo()!=null){
            System.out.print("("+pacote.getGrupo().getNome()+")");
        }else{
            System.out.println("(Privado)");
        }
        System.out.print("["+mensagem.getHora()+"]");
        if (mensagem.getRemetente().getAddress().equals(eu.getAddress())){
            System.out.print(" Você: " );
        }else{
            System.out.print( mensagem.getRemetente().getNickname() + ": " );
        }
        System.out.println( mensagem.getMensagem());
    }

    public void atualizaDados() {

        Pacote pacote = new Pacote(null, listaDeContatos, listaDeGrupos, conversas, Tag.ATUALIZA_DADOS, null);
        Message message = new Message(null, pacote);
        try {
            System.out.println("Atualizando base dados...");
            canal.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public Grupo escolheGrupo(){

        List<String> grupos = new ArrayList<>();
        grupos.addAll(listaDeGrupos.keySet());
        Grupo grupo = null;
        Integer op;

        if (grupos.size() > 0){
            while (grupo == null){
                System.out.println("Selecione um grupo para enviar mensagens:");
                for (int i=0; i < grupos.size(); i++) {
                    System.out.println("("+i+") "+listaDeGrupos.get(grupos.get(i)).getNome()+" -> Coordenador:"+ listaDeGrupos.get(grupos.get(i)).getCoordenador().getNickname());
                }
                op = leNumeroTeclado();
                if (op != null){
                    if ((op >= 0)&&(op < grupos.size())){
                        grupo = listaDeGrupos.get(grupos.get(op));
                    }else {
                        System.out.println("Opção inválida");
                    }
                }
            }
            System.out.println("Retornando grupo do coordenador: "+grupo.getCoordenador());
            return grupo;
        }else{
            System.out.println("Não há grupos disponível.");
            return null;
        }
    }

    public Usuario escolheAmigo(){

        List<String> listaDeNicks = new ArrayList<>();
        listaDeNicks.addAll(listaDeContatos.keySet());
        Usuario amigo = null;
        String op;

        while (amigo == null){
            System.out.println("Selecione um amigo para enviar mensagens:");
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

                // escolhe um grupo para enviar a mensagem
                Grupo grupo = escolheGrupo();

                if (grupo != null){
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    String line;

                    // imprime as mensagens anteriores do grupo
                    imprimeMensagensDoGrupo(grupo);

                    System.out.print("Use 'quit' ou 'exit' para sair da conversa.");
                    while (true) {
                        try {
                            System.out.print("> ");
                            System.out.flush();
                            line = in.readLine().toLowerCase();
                            if (line.startsWith("quit") || line.startsWith("exit")) {
                                break;
                            }

                            //System.out.println("["+getTime()+"]" + eu.getNickname() + ": " +line);
                            enviaAnycast(grupo,line);
                            //salvaHistoricoOffline(grupo , "["+getTime()+"]" + eu.getNickname() + ": " +line);

                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                            e.printStackTrace();
                        }
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
        //this.listaDeGrupos.put(eu.getNickname(), grupo);
        this.listaDeGrupos.put(nomeGrupo, grupo);

        System.out.println("Grupo '"+nomeGrupo+"' criado com sucesso!");

        atualizaDados();

        // retorna o grupo
        return grupo;
    }

    public void verGrupos(){

        System.out.println("Foram encontrados "+listaDeGrupos.size()+" grupo(s)");

        List<Grupo> grupos = new ArrayList<>(listaDeGrupos.values());

        for (Grupo grupo : grupos) {
            System.out.print("Nome: '"+grupo.getNome() + "' Coord. -> '" +grupo.getCoordenador().getNickname()+"'");
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

    public boolean ehCoordenadorDoGrupo(Usuario usuario, Grupo grupo){

        return Objects.equals(usuario.getNickname(), grupo.getCoordenador().getNickname());

    }

    public void sobre(){
        System.out.println("\n***  JGroups Chat v1.0  ***\n" +
                "\n" +
                "Sistema distribuído (SD) como um serviço de chat, \n" +
                "inspirado em aplicativos populares como WhatsApp, \n" +
                "Telegram e afins. \n" +
                "\n" +
                "O trabalho está em processo de desenvolvimento e ao \n" +
                "ser finalizado, continuará sendo de código aberto. \n" +
                "Qualquer estudo que venha ser feito apartir deste \n" +
                "trabalho, os autores envolvidos devem ser devidamente \n" +
                "referenciados.\n" +
                "  \n" +
                "  \n" +
                "Autores:\n" +
                "        Matheus Calixto, Samuel Terra\n");
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

    public void salvaHistoricoOffline() {

        try {
            File file = new File("historico_offline");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(conversas);
            s.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public void leHistoricoOffline() {

        try {
            File file = new File("historico_offline");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            HashMap<String, Object> fileObj2 = (HashMap<String, Object>) s.readObject();
            s.close();

            System.out.println(fileObj2);
        }catch (IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean verificaHistoricoSalvoGrupo(Grupo grupo){
        return conversas.containsKey(grupo.getNome());
    }

    /**
     * Método responsável por exibir o histórico de todos os grupos
     * */
    public void verHistoricoDeTodasConversas(){

        List<String> nomeDasConversas = new ArrayList<>(conversas.keySet());

        //System.out.println("DEBUG: Todoo o keyset: "+ nomeDasConversas);

        for (String conversa : nomeDasConversas) {
            List<String> historicoDeUmaConversa = conversas.get(conversa);

            System.out.println("\n\nHistórico da conversa '"+conversa+"':");

            for (int i = 0; i < historicoDeUmaConversa.size(); i++){
                System.out.println(historicoDeUmaConversa.get(i));
            }
        }

    }

    /**
     * Método responsável por verificar se um grupo existe
     * */
    public boolean grupoExiste(Grupo grupo){
        return listaDeGrupos.containsKey(grupo.getNome());
    }

    /**
     * Método responsável por verificar se um usuário existe
     * */
    public boolean usuarioExiste(Usuario usuario){
        return listaDeContatos.containsKey(usuario.getNickname());
    }

    public void apagaGrupo(){

        // seleciona o grupo que deseja apagar
        Grupo grupo = escolheGrupo();

        // verifica se o usuario pertence ao grupo
        if (pertenceAoGrupo(eu, grupo)){
            // somente coordenador pode apagar o grupo
            if (ehCoordenadorDoGrupo(eu, grupo)){
                // apaga a grupo da hash de grupos
                listaDeGrupos.remove(grupo.getNome());
                // atualiza os dados
                atualizaDados();
                // apaga o historico do grupo
                if (conversas.containsKey(grupo.getNome())){
                    conversas.remove(grupo.getNome());
                    System.out.println("Grupo "+grupo.getNome()+" removido com sucesso.");
                }else{
                    System.out.println("O grupo não possuia historico.");
                }
            }else{
                System.out.println("Você não pode realizar essa operação porque não coordenados do grupo.");
            }
        }else{
            System.out.println("Você não pertence a esse grupo.");
        }

    }

    /**
     * Método responsável por renomear um grupo
     * */
    public void renomearGrupo(){

        // seleciona o grupo que deseja apagar
        Grupo grupo = escolheGrupo();

        // verifica se o usuario pertence ao grupo
        if (pertenceAoGrupo(eu, grupo)){
            // somente coordenador pode apagar o grupo
            if (ehCoordenadorDoGrupo(eu, grupo)){

                // remove e guarda o grupo e o historico das hashs
                grupo = listaDeGrupos.remove(grupo.getNome());
                List<String> c = conversas.remove(grupo.getNome());

                String nomeGrupo = null;

                System.out.println("Informe o novo nome do grupo.");
                while(nomeGrupo == null){
                    // obtem o novo nome do grupo
                    nomeGrupo = leTextoTeclado();
                }

                // seta o nome do grupo para um novo nome
                grupo.setNome(nomeGrupo);

                // adiciona o grupo e a conversa com nomes novos novamente na hash
                listaDeGrupos.put(grupo.getNome(), grupo);
                conversas.put(nomeGrupo, c);

                // atualiza os dados
                atualizaDados();
            }else{
                System.out.println("Você não pode realizar essa operação porque não coordenados do grupo.");
            }
        }else{
            System.out.println("Você não pertence a esse grupo.");
        }
    }

    /**
     * Método responsável por exibir o historico de conversar de um determinado grupo
     * */
    public void imprimeMensagensDoGrupo(Grupo grupo){

        if (conversas.containsKey(grupo.getNome())){
            List<String> mensagens = conversas.get(grupo.getNome());

            for (String mensagem : mensagens) {
                System.out.println(mensagem);
            }

        }else {
            System.out.println("Não há mensagens anteriores.");
        }

    }

    public void adicionaAmigoNoGrupo(){

    }

    public void menuPrincipal() throws Exception {

        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("\n***  JGroups Chat v1.0  ***\n");
        menuPrincipal.append("\n***    Menu Principal  ***\n");
        menuPrincipal.append("Selecione uma opção:\n\n");
        menuPrincipal.append("1. Ficar online/offline\n");
        menuPrincipal.append("2. Mudar/definir nickname\n");
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
                        verAmigosOnline();
                        break;
                    }
                    case (4):{
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
                        sobre();
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
        menuPrincipal.append("3. Enviar mensagem a um grupo\n");
        menuPrincipal.append("4. Apagar grupo\n");
        menuPrincipal.append("5. Renomear grupo\n");
        menuPrincipal.append("6. Adicionar amigo ao grupo\n");
        menuPrincipal.append("7. Ver detalhes de um grupo\n");
        menuPrincipal.append("8. Ver historicos\n");
        menuPrincipal.append("9. Voltar\n\n");

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
                    verGrupos();
                    break;
                }
                case (3): {
                    enviaMensagemGrupo();
                    break;
                }
                case (4): {
                    apagaGrupo();
                    break;
                }
                case (5): {
                    renomearGrupo();
                    break;
                }
                case (6): {
                    adicionaAmigoNoGrupo();
                    break;
                }
                case (7): {
                    detalhesGrupo();
                    break;
                }
                case (8): {
                    verHistoricoDeTodasConversas();
                    break;
                }
                case (9): {
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

        Pacote p = new Pacote(new Mensagem(null, eu, conteudo, getTime()), listaDeContatos, listaDeGrupos, conversas, Tag.MENSAGEM_ANYCAST, g);

        //Message mensagem = new Message(null, "{ ANYCAST } " + conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados
        Message mensagem = new Message(null, p); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_NONE); // espera receber a resposta da maioria do grupo (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(true);

        RspList respList = this.despachante.castMessage(grupo, mensagem, opcoes); //ANYCAST

        System.out.println("==> Respostas do grupo ao ANYCAST:\n" +respList+"\n");

    }

}


    /*

    GET_ALL: block until responses from all members (minus the suspected ones) have been received.
    GET_NONE: wait for none. This makes the call non-blocking
    GET_FIRST: block until the first response (from anyone) has been received
    GET_MAJORITY: block until a majority of members have responded

    */