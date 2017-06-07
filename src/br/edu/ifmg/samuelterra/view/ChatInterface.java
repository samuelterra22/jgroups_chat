package br.edu.ifmg.samuelterra.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by samuel on 06/06/17.
 */
public class ChatInterface extends JFrame implements WindowListener, MouseListener, KeyListener {

    private TextArea messageArea    = null;
    private TextField sendArea      = null;
    private String userName         = "Samuel";

    public ChatInterface(String s) {
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

        Panel p = new Panel();
        p.setLayout(new FlowLayout());

        sendArea = new TextField(30);
        sendArea.addKeyListener(this);
        sendArea.setFont(new Font("Arial", Font.PLAIN, 16));

        p.add(sendArea);
        p.setBackground(new Color(221,221,221));
        Button send = new Button("Send");
        send.addMouseListener(this);
        p.add(send);
        Button clear = new Button("Clear");
        clear.addMouseListener(this);
        p.add(clear);

        JLabel labelUser = new JLabel("Usuário: "+userName);
        p.add(labelUser);

        this.add(p, "South");
        this.setVisible(true);
        sendArea.requestFocus();

    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

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
