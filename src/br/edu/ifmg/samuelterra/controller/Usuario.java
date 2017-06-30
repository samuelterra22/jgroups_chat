package br.edu.ifmg.samuelterra.controller;


import org.jgroups.Address;

import java.io.Serializable;

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
