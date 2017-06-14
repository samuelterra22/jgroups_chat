package br.edu.ifmg.samuelterra;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by samuel on 06/06/17.
 */
public class ChatInterface extends JFrame implements WindowListener {

    private TextArea messageArea;
    private TextField sendArea;
    private String userName;

    // windows builder

    public ChatInterface(String s)  {
        super(s);

        this.addWindowListener(this);
        this.setSize(800,600);
        this.setResizable(true);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageArea = new TextArea();
        messageArea.setEditable(false);
        this.add(messageArea, "Center");
        messageArea.setFont(new Font("Arial", Font.PLAIN, 16));

        sendArea = new TextField(30);
        sendArea.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                keySendAresPressed();
            }
        });
        sendArea.setFont(new Font("Arial", Font.PLAIN, 16));

        Button send = new Button("Send");
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendButtonPressed();
            }
        });
        Button clear = new Button("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearButtonPressed();
            }
        });

        //if (userName == null)
        //    this.userName = JOptionPane.showInputDialog("Escolha um nickname para usar o bate papo");

        JLabel labelUser = new JLabel("Usuário: "+"Samuel");

        Panel p = new Panel();
        p.setLayout(new FlowLayout());
        p.add(sendArea);
        p.setBackground(new Color(221,221,221));  // Color.decode("#E0E4CC")
        p.add(send);
        p.add(clear);
        p.add(labelUser);

        this.add(p, "South");

        this.setVisible(true);
        sendArea.requestFocus();

    }

    private void keySendAresPressed(){
        messageArea.append(this.userName+": "+sendArea.getText()+"\n");
        sendArea.setText(" ");
    }

    private void sendButtonPressed(){
        messageArea.append(this.userName+": "+sendArea.getText()+"\n");
        sendArea.setText(" ");
    }

    private void clearButtonPressed(){
        sendArea.setText(" ");
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {

    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}