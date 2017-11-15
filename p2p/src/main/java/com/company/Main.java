package com.company;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    static ArrayList<User> userList = new ArrayList<User>();
    static DataOutputStream outToClient;
    public static void main(String[] args) throws IOException {
        String clientSentence;
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(5353);
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            analyzeInput(clientSentence);
        }

    }
    public static void analyzeInput(String text){
        System.out.println(text);
        JSONObject jsonObject = new JSONObject(text);
        int type = jsonObject.getInt("type");
        switch (type){
            case 1: doRegistration(jsonObject); break;
            case 2: getUSerByUserName(jsonObject.getString("userName")).setActive(false); break;
            case 3: searchUser(jsonObject); break;
            case 5: deleteUser(jsonObject);
        }
    }
    public static void doRegistration(JSONObject jsonObject){
        String userName = jsonObject.getString("userName");
        String pass = jsonObject.getString("pass");
        String ip = jsonObject.getString("ip");
        if(checkIfUserNameFair(userName)){
            JSONObject obj = new JSONObject();
            obj.put("success","1");
            obj.put("message","");
            sendData(obj.toString());
            userList.add(new User(userName,pass,ip,true));
        }else{
            JSONObject obj = new JSONObject();
            obj.put("success","0");
            obj.put("message","UserName taken");
            sendData(obj.toString());
        }
    }
    public static boolean checkIfUserNameFair(String userName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).getUserName().equals(userName))
                return false;
        }
        return true;
    }
    public static void sendData(String text){
        try{
            text = text + '\n';
            outToClient.writeBytes(text);
            System.out.println("gönderilen :"+text);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static User getUSerByUserName(String userName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).getUserName().equals(userName))
                return userList.get(i);
        }
        return null;
    }
    public static void searchUser(JSONObject jsonObject){
        JSONObject jsonObjectTemp = new JSONObject();
        User u = getUSerByUserName(jsonObject.getString("userName"));
        if(u==null){
            jsonObjectTemp.put("success","0");
            sendData(jsonObjectTemp.toString());
        }else{
            String ip = u.getIp();
            jsonObjectTemp.put("ipAdres",ip);
            jsonObjectTemp.put("success","1");
            sendData(jsonObjectTemp.toString());
        }

    }
    public static void deleteUser(JSONObject jsonObject){
        JSONObject jsonObjectTemp = new JSONObject();
        User u = getUSerByUserName(jsonObject.getString("userName"));
        if(u!=null)
        userList.remove(u);
        jsonObjectTemp.put("success","1");
        sendData(jsonObjectTemp.toString());
    }
}
