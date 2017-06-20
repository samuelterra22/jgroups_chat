package br.edu.ifmg.samuelterra.controller;


import br.edu.ifmg.samuelterra.model.Grupo;

import java.util.ArrayList;
import java.util.List;


public class GrupoController {

    public GrupoController() {    }

    public List<String> getListaDeNicknames(Grupo grupo){

        List<Usuario> usuarios = grupo.getUsuarios();
        List<String> listaDeNicknames = new ArrayList<>();

        for (Usuario usuario: usuarios) {
            listaDeNicknames.add(usuario.getNickname());
        }

        return listaDeNicknames;

    }

}
