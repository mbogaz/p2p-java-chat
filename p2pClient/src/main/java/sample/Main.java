package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Main extends Application {
    public static Controller controller;
    public static String serverIp = "10.20.13.59";
    public static String chatIp = "";
    public static int serverPort= 5353;
    public static int chatPort= 3535;
    public static boolean isConnected = false;
    public static DataOutputStream outToClient;
    public static String userName = "";
    public static Socket connectionSocket;
    public static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainScreen.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Welcome");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controller.sendLeaveMessage();
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        Runnable myRunnable = () -> {
            try {
                String clientSentence;
                String capitalizedSentence;
                ServerSocket welcomeSocket = new ServerSocket(chatPort);
                while (true) {
                    connectionSocket = welcomeSocket.accept();
                    BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    clientSentence = inFromClient.readLine();
                    analyzeInput(clientSentence);
                }
            }catch(Exception e){}
        };
        Thread thread = new Thread(myRunnable);
        thread.start();

        launch(args);
    }
    public static void analyzeInput(String text){
        JSONObject jsonObject = new JSONObject(text);
        int type = jsonObject.getInt("type");
        switch (type){
            case 1:
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Chat Request");
                    alert.setContentText("Do you want chat with "+jsonObject.getString("userName"));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        JSONObject jsonObjectTemp = new JSONObject();
                        jsonObjectTemp.put("success","1");
                        sendData(jsonObjectTemp.toString());
                        controller.startChat(jsonObject.getString("ipAdress"));
                    } else {
                        JSONObject jsonObjectTemp = new JSONObject();
                        jsonObjectTemp.put("success","0");
                        sendData(jsonObjectTemp.toString());
                    }
                });
                break;
            case 2:
                String userName = jsonObject.getString("userName");
                String mssg = jsonObject.getString("message");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.getChatController().addMessageToFlow(userName,mssg);
                    }
                });
                break;
        }
    }
    public static void sendData(String text){
        try{
            text = text + '\n';
            outToClient.writeBytes(text);
            System.out.println("gönderilen :"+text);
            connectionSocket.close();
        }catch(Exception e){
            JSONObject jsonObject = new JSONObject(text);
            System.out.println(e.getMessage()+" message :"+jsonObject.toString());
        }
    }
    public static void setText(String text){
        primaryStage.setTitle(text);
    }
}
