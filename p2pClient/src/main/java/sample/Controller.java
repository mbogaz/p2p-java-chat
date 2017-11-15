package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    GridPane chatRoot;
    Scene scene;
    Stage stage;
    @FXML Button btnJoin,btnSearch;
    @FXML Label infoLabel;
    ChatController chatController;
    public static String selfIp;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/chatScreen.fxml"));
            chatRoot = chatLoader.load();
            chatController = chatLoader.getController();
            scene = new Scene(chatRoot);
            stage = new Stage();
        }catch (Exception e){}

        btnJoin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getLoginInfo();
            }
        });
        btnSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(Main.isConnected == false){
                    JOptionPane.showMessageDialog(null,"Join First");
                    return;
                }
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Search User");
                dialog.setContentText("User Name:");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()){
                    String ip = getUserIp(result.get());
                    if(ip.equals("")){
                        JOptionPane.showMessageDialog(null,"NOT FOUND");
                        return;
                    }
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Start chat");
                    alert.setHeaderText("You will chat with "+result.get()+". Do you agree?");
                    Optional<ButtonType> result2 = alert.showAndWait();
                    if (result2.get() == ButtonType.OK){
                        sendChatRequest(ip);
                    }
                }

            }
        });
    }
    public void getLoginInfo(){
        if(Main.isConnected){
            JOptionPane.showMessageDialog(null,"You Already Connected");
            return ;
        }
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

// Set the icon (must be included in the project).
        dialog.setGraphic(new ImageView(this.getClass().getResource("/login-icon.png").toString()));

// Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

// Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            if(password.getText().equals("")){
                JOptionPane.showMessageDialog(null,"Enter Password");
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName",username.getText());
            jsonObject.put("pass",password.getText());
            jsonObject.put("type","1");
            String ipAdress = "";
            try{
                InetAddress[] localaddr;
                Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
                for (; n.hasMoreElements();)
                {
                    NetworkInterface e = n.nextElement();

                    Enumeration<InetAddress> a = e.getInetAddresses();
                    for (; a.hasMoreElements();)
                    {
                        InetAddress addr = a.nextElement();
                        if(addr.isSiteLocalAddress())
                            ipAdress = addr.getHostAddress();
                    }
                }
            }catch (Exception e){
                System.out.println("Error while getting ip, Error message :"+e.getMessage());
            }
            selfIp = ipAdress;
            Main.setText(username.getText());
            jsonObject.put("ip",ipAdress);
            sendJoinRequest(jsonObject.toString(),username.getText());
        });

    }
    public void sendJoinRequest(String text,String userName){
        JSONObject jsonObject = null;
        try {
            jsonObject = sendData(text,Main.serverPort);
            if(jsonObject == null){
                return;
            }
        }catch (Exception e){}
            if(jsonObject!=null && jsonObject.getInt("success")==1){
                infoLabel.setStyle("-fx-background-color: green");
                infoLabel.setText("Connected");
                Main.userName = userName;
                Main.isConnected = true;
            }else
                JOptionPane.showMessageDialog(null,jsonObject.getString("message"));
    }
    public String getUserIp(String userName){
        String ipAdres="";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName",userName);
        jsonObject.put("type","3");
        try{
            String modifiedSentence;
            Socket clientSocket = new Socket(Main.serverIp, Main.serverPort);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(jsonObject.toString()+ '\n');
            modifiedSentence = inFromServer.readLine();
            jsonObject = new JSONObject(modifiedSentence);
            clientSocket.close();
        }catch (Exception e){}
            if(jsonObject!=null && jsonObject.getInt("success")==1){
                ipAdres = jsonObject.getString("ipAdres");
            }
        return ipAdres;
    }
    public void startChat(String ip){
        if(stage.isShowing())
            return;
            Main.chatIp = ip;
            stage.setTitle("Chat");
            stage.setScene(scene);
            stage.show();
    }
    public void sendChatRequest(String ip){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName",Main.userName);
        jsonObject.put("ipAdress",selfIp);
        jsonObject.put("type","1");
        try {
            String text = jsonObject.toString() + '\n';
            JSONObject jsonObject2 = sendData(text,Main.chatPort);
            if(jsonObject2.getInt("success")==1){
                startChat(ip);
            }else{
                JOptionPane.showMessageDialog(null,"User rejected");
            }
        }catch (Exception e){}

    }
    public ChatController getChatController(){
        return chatController;
    }
    public JSONObject sendData(String text,int port){
        JSONObject returnData = null;
        try{
            text = text + '\n';
            System.out.println("servera giden data :"+text);
            Socket clientSocket = new Socket(Main.serverIp, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(text);
            String modifiedSentence = inFromServer.readLine();
            System.out.println("serverdan gelen data :"+modifiedSentence);
            returnData = new JSONObject(modifiedSentence);
            clientSocket.close();
        }catch(Exception e){
            System.out.println("veri gönderme problemi : "+e.getMessage());
        }
        if(returnData == null)
            JOptionPane.showMessageDialog(null,"Connection Error");
        return returnData;
    }
    public void sendLeaveMessage(){
        JSONObject jsonObjectTemp = new JSONObject();
        jsonObjectTemp.put("type","5");
        jsonObjectTemp.put("userName",Main.userName);
        sendData(jsonObjectTemp.toString(),Main.serverPort);
    }
}
