package com.example.profiscooter;

public class User {

    public String nick, age, email;

    public User(){

    }

    public User(String nick, String age, String email){
        this.nick = nick;
        this.age = age;
        this.email = email;
    }

    public String getNick(){
        return nick;
    }

    public String getEmail(){
        return email;
    }

    public String getAge(){
        return age;
    }
}
