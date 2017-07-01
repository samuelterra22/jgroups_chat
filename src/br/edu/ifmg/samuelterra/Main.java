package br.edu.ifmg.samuelterra;


import br.edu.ifmg.samuelterra.view.Principal;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws Exception {
        //Random gerador = new Random();
        // new UsuarioChat("ChatDistribuido", "Samuel" + gerador.nextFloat()).start();

        Principal m = new Principal();
        m.menuPrincipal();

        /*HashMap<String, Object> fileObj = new HashMap<String, Object>();

        ArrayList<String> cols1 = new ArrayList<String>(); cols1.add("a"); cols1.add("b"); cols1.add("c");
        ArrayList<String> cols2 = new ArrayList<String>(); cols2.add("e"); cols2.add("f"); cols2.add("g");
        ArrayList<String> cols3 = new ArrayList<String>(); cols3.add("h"); cols3.add("i"); cols3.add("j");

        fileObj.put("mylist1", cols1);
        fileObj.put("mylist2", cols2);
        fileObj.put("mylist3", cols3);
        {
            File file = new File("temp");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(fileObj);
            s.close();
        }
        File file = new File("temp");
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        HashMap<String, Object> fileObj2 = (HashMap<String, Object>) s.readObject();
        s.close();

        System.out.println(fileObj2.get("mylist2"));*/

    }





}
