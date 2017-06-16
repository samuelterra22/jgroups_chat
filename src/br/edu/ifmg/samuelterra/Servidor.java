package br.edu.ifmg.samuelterra;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Servidor extends ReceiverAdapter {
    JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");
    final List<String> state = new LinkedList<String>();

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        String line = msg.getSrc() + ": " + msg.getObject();

        System.out.println(line);

        synchronized (state) {
            state.add(line);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        /**
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
        System.out.println("received state (" + list.size() + " messages in chat history):");
        for (String str : list) {
            System.out.println(str);
        }
    }


    private void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                line = "[" + user_name + "] " + line;
                Message msg = new Message(null, line);
                channel.send(msg);
            } catch (Exception e) {
            }
        }
    }


    public static void main(String[] args) throws Exception {
        new Servidor().start();
    }
}
