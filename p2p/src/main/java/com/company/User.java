package com.company;

/**
 * Created by mahmut on 30.07.2016.
 */
public class User {
    private String userName;
    private String passWord;
    private String ip;
    private boolean isActive;

    public User(String userName, String passWord,String ip,boolean isActive) {
        this.userName = userName;
        this.passWord = passWord;
        this.ip = ip;
        this.isActive = isActive;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}


