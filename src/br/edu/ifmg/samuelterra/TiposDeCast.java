package br.edu.ifmg.samuelterra;

/*
 SAIBA MAIS: http://www.jgroups.org/manual/html/user-building-blocks.html#MessageDispatcher
/**/

import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;

import java.util.*;

public class TiposDeCast extends ReceiverAdapter implements RequestHandler {

    JChannel canalDeComunicacao;
    MessageDispatcher  despachante;
    final int TAMANHO_MINIMO_CLUSTER = 4;

    public static void main(String[] args) throws Exception {
        new TiposDeCast().start();
    }

    private void start() throws Exception {

        //Cria o canal de comunicação com uma configuração padrão do JGroups
        //canalDeComunicacao=new JChannel("udp.xml");
        //canalDeComunicacao=new JChannel("cast.xml");
        canalDeComunicacao=new JChannel();

        despachante=new MessageDispatcher(canalDeComunicacao, null, null, this);

        canalDeComunicacao.setReceiver(this);	//quem irá lidar com as mensagens recebidas

        canalDeComunicacao.connect("TiposDeCast");
        eventLoop();
        canalDeComunicacao.close();
    }

    private void eventLoop() {

        Address meuEndereco = canalDeComunicacao.getAddress();

        while( canalDeComunicacao.getView().size() < TAMANHO_MINIMO_CLUSTER )
            Util.sleep(100); // aguarda os membros se juntarem ao cluster

        Vector<Address> cluster = new Vector<Address>(canalDeComunicacao.getView().getMembers());
        Address primeiroMembro = cluster.elementAt(0);  //0 a N
        Address segundoMembro  = cluster.elementAt(1);  //0 a N
        Address ultimoMembro   = cluster.lastElement();
        // Atenção, o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)

        Vector<Address> grupo = new Vector<Address>();
        grupo.add(segundoMembro);
        grupo.add(ultimoMembro);

        if( meuEndereco.equals(primeiroMembro) ) {  // somente o primeiro membro envia o getListaDeNicknames abaixo

            try {
                enviaUnicast( segundoMembro, "O segundo membro concorda?" ); //envia unicast para o primeiro

                enviaAnycast( grupo, "Os membros do grupo concordam?" ); //envia anycast para o primeiro e o último

                enviaMulticast( "Todos concordam?" ); //envia multicast para todos
            }
            catch(Exception e) {
                System.err.println( "ERRO: " + e.toString() );
            }

        } // if primeiro
        else{
            while( canalDeComunicacao.getView().getMembers().contains(primeiroMembro) )
                Util.sleep(100); // aguarda o primeiro membro sair do cluster

            System.out.println("\nBye bye...");
        }

    }//eventLoop

    private RspList enviaMulticast(String conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Address cluster = null; //endereço null significa TODOS os membros do cluster
        Message mensagem=new Message(cluster, "{MULTICAST} "+conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_ALL); // espera receber a resposta de TODOS membros (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //MULTICAST
        System.out.println("==> Respostas do cluster ao MULTICAST:\n" +respList+"\n");

        return respList;
    }

    private RspList enviaAnycast(Collection<Address> grupo, String conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(null, "{ ANYCAST } " + conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_MAJORITY); // espera receber a resposta da maioria do grupo (ALL, MAJORITY, FIRST, NONE)

        opcoes.setAnycasting(true);

        RspList respList = despachante.castMessage(grupo, mensagem, opcoes); //ANYCAST
        System.out.println("==> Respostas do grupo ao ANYCAST:\n" +respList+"\n");

        return respList;
    }

    private String enviaUnicast(Address destino, String conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(destino, "{ UNICAST } " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setFlags(Message.DONT_BUNDLE); // envia imediatamente, não agrupa várias mensagens numa só
        opcoes.setMode(ResponseMode.GET_FIRST); // não espera receber a resposta do destino (ALL, MAJORITY, FIRST, NONE)

        String resp = despachante.sendMessage(mensagem, opcoes); //UNICAST
        System.out.println("==> Respostas do membro ao UNICAST:\n" +resp+"\n");

        return resp;
    }

    public void receive(Message msg) { //exibe mensagens recebidas
        System.out.println("" + msg.getSrc() + ": " + msg.getObject());
    }

    public Object handle(Message msg) throws Exception{ // responde requisições recebidas
        String pergunta = (String) msg.getObject();
        System.out.println("RECEBI uma mensagem: " + pergunta+"\n");

        if(pergunta.contains("concorda"))
            return " SIM "; //resposta à requisição contida na mensagem
        else
            return " NÃO ";
    }

    public void viewAccepted(View new_view) { //exibe alterações na composição do grupo
        System.out.println("\t** nova View do cluster: " + new_view);
    }


}//class
