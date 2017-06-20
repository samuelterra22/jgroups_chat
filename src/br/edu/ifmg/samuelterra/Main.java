package br.edu.ifmg.samuelterra;


import br.edu.ifmg.samuelterra.view.Menu;

import java.util.Random;


public class Main {

    public static void main(String[] args) throws Exception {
        Random gerador = new Random();
        //new UsuarioChat("ChatDistribuido", "Samuel" + gerador.nextFloat()).start();

        Menu m = new Menu();
        m.menuPrincipal();

    }
}
