package br.edu.ifmg.samuelterra;

import java.util.List;

public class Grupo {

    private String groupName;

    private List<UsuarioChat> usuarios;

    public Grupo(String groupName, List<UsuarioChat> usuarios) {
        this.groupName = groupName;
        this.usuarios = usuarios;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<UsuarioChat> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioChat> usuarios) {
        this.usuarios = usuarios;
    }
}
