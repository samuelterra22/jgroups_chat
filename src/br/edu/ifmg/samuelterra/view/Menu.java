package br.edu.ifmg.samuelterra.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Menu {

    public Menu() {    }

    public void menuPrincipal() throws IOException {

        StringBuffer menuPrincipal = new StringBuffer();
        menuPrincipal.append("***  JGroups Chat v1.0  ***\n\n");
        menuPrincipal.append("Selecione uma opção:\n");
        menuPrincipal.append("1. Ficar on-line\n");
        menuPrincipal.append("2. Ver lista de contatos\n");
        menuPrincipal.append("3. Enviar mensagem para um amigo\n");
        menuPrincipal.append("4. Grupos\n");
        menuPrincipal.append("5. Sair\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int opcao = 0;

        while (opcao != 5){
            System.out.println(menuPrincipal);
            System.out.flush();
            opcao = Integer.parseInt(in.readLine());
            switch (opcao){
                case (1):{
                    System.out.println("Ficar on-line");
                    break;
                }
                case (2):{
                    System.out.println("Ver lista de contatos");
                    break;
                }
                case (3):{
                    System.out.println("Enviar mensagem para um amigo");
                    break;
                }
                case (4):{
                    System.out.println("Grupos");
                    break;
                }
                case (5):{
                    System.out.println("Bye bye...");
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
