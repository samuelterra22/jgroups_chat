package br.edu.ifmg.samuelterra.model;


import org.jgroups.Address;

import java.io.Serializable;

/***********************************************************************************************************************
 *                                         JGroups Chat v1.0                                                           *
 *   Alunos:        Matheus Calixto | Samuel Terra                                                                     *
 *   Professor:     Everthon Valadão                                                                                   *
 *   Disciplina:    Sistemas Distribuidos                                                                              *
 *   Modificado em: 03/07/2017                                                                                         *
 *                                                                                                                     *
 **********************************************************************************************************************/
public class Usuario implements Serializable {

    private String nickname;
    private Address address;

    public Usuario(String nickname, Address address) {
        this.nickname = nickname;
        this.address = address;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nickname='" + nickname + '\'' +
                ", address=" + address +
                '}';
    }
}
