package br.edu.ifmg.samuelterra.view;

import br.edu.ifmg.samuelterra.controller.CriaMensagem;
import br.edu.ifmg.samuelterra.controller.Usuario;
import br.edu.ifmg.samuelterra.model.Mensagem;
import br.edu.ifmg.samuelterra.model.Pacote;
import br.edu.ifmg.samuelterra.model.Tag;
import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;
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

    private Usuario eu;

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

            System.out.println("["+msg.getHora()+"]" + msg.getRemetente().getNickname() + ": " + msg.getMensagem());

            listaDeContatos.put(msg.getRemetente().getNickname(), pacote.getSrc());

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
            System.out.println("Atualizando lista de contatos...");
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

    public Usuario escolheAmigo(){

        List<String> listaDeNicks = new ArrayList<>();
        listaDeNicks.addAll(listaDeContatos.keySet());
        Usuario amigo = null;
        String op;

        while (amigo == null){
            System.out.println("Selecione um amigo para iniciar a conversa:");
            for (int i=0; i < listaDeNicks.size(); i++) {
                if (listaDeContatos.get(listaDeNicks.get(i)) == eu.getAddress()){
                    System.out.println("("+i+") "+ listaDeNicks.get(i)+" (Você)");
                }else {
                    System.out.println("("+i+") "+ listaDeNicks.get(i));
                }
            }
            op = leTextoTeclado();
            if (op != null){
                if ((Integer.parseInt(op) >= 0)&&(Integer.parseInt(op) < listaDeNicks.size())){
                    amigo = new Usuario(listaDeNicks.get(Integer.parseInt(op)), listaDeContatos.get(Integer.parseInt(op)));
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
                        Message msg = new CriaMensagem().criaMulticast(eu, "teste",getTime(), listaDeContatos);

                        canal.send(msg);

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
                System.out.print("Use 'quit' ou 'exit' para sair da conversa.");
                while (true) {
                    try {
                        System.out.print("> ");
                        System.out.flush();
                        line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit")) {
                            break;
                        }

                        Message msg = new CriaMensagem().criaUnicast(amigo, eu, line, getTime(), listaDeContatos);

                        canal.send(msg);

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

    public void menuPrincipal() throws Exception {

        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("\n***  JGroups Chat v1.0  ***\n");
        menuPrincipal.append("Selecione uma opção:\n\n");
        menuPrincipal.append("1. Ficar online/offline\n");
        menuPrincipal.append("2. Mudar nickname\n");
        menuPrincipal.append("3. Ver lista de amigos\n");
        menuPrincipal.append("4. Enviar mensagem para um amigo\n");
        menuPrincipal.append("5. Gerenciar grupos\n");
        menuPrincipal.append("6. Sobre\n");
        menuPrincipal.append("7. Sair\n\n");

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
                    //escolheAmigo();
                    break;
                }
                case (5):{
                    System.out.println("Grupos");
                    enviaMensagemGrupo();
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
