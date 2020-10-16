package ru.stk.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.stk.client.Client;

import java.io.IOException;

public class FXController {
    Stage curStage;
    Scene mainScene;
    FileChooser fileChooser = new FileChooser();
    Client client = new Client();

    @FXML
    public void setCurStage(Stage stage){

        curStage = stage;
    }

    @FXML
    public void setMainScene(Scene scene){
        mainScene = scene;
    }

    @FXML
    private void login (ActionEvent event){
        try{
            client.connect();
        }
        catch(InterruptedException | IOException e){
            /* TODO: Handle this exception, write log, inform user */
            e.printStackTrace();
        };

        curStage.setScene(mainScene);
        //Window curWindow = curStage.getOwner();
        //fileChooser.showOpenDialog(curWindow);
    }

    @FXML
    private void exit (ActionEvent event){
        System.exit(0);
    }

    @FXML
    private void uploadFile (ActionEvent event) {

    }


}
