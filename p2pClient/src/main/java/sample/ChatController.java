package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by mahmut on 17.08.2016.
 */
public class ChatController implements Initializable {
    @FXML Button btnSend;
    public @FXML TextArea messageArea;
    @FXML ScrollPane messageScrool;
    @FXML Label flowLabel;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageScrool.vvalueProperty().bind(flowLabel.heightProperty());
        messageArea.setFocusTraversable(false);
        messageArea.setOnKeyReleased(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
                sendMessage();
        });
        btnSend.setOnAction(event -> sendMessage());
    }
    public void addMessageToFlow(String userName,String mssg){
        if(flowLabel.getText().equals("")){
            flowLabel.setText(userName+": "+mssg);
            return;
        }
        flowLabel.setText(flowLabel.getText()+"\n"+userName+": "+mssg);
    }
    public void sendMessage(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName",Main.userName);
        jsonObject.put("message",messageArea.getText());
        jsonObject.put("type","2");
        try {
            Socket clientSocket = new Socket(Main.chatIp, Main.chatPort);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            String text = jsonObject.toString() + '\n';
            outToServer.writeBytes(text);
            clientSocket.close();
        }catch (Exception e){
            System.out.println("send message error :"+e.getMessage());
        }
        addMessageToFlow("you",messageArea.getText());
        messageArea.setText("");
    }
}
