package ru.stk.gui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.stk.client.Client;
import ru.stk.client.UserFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LoginFxCtl {


    private MainFxCtl mainFxCtl;
    private Stage curStage;
    private Scene mainScene;
    private final FileChooser fileChooser = new FileChooser();
    private final Client client = new Client();

    @FXML
    private TextField loginTFiled;
    @FXML
    private TextField passTFiled;

    public void setCurStage(Stage stage){
        curStage = stage;
    }

    public void setMainScene(Scene scene){
        mainScene = scene;
        loginTFiled.setText("Serge");
        passTFiled.setText("Serge7");
    }

    public void setMainCtl(MainFxCtl ctl){
        mainFxCtl = ctl;
    }

    @FXML
    private void login (ActionEvent event){

        try{
            client.connect(mainFxCtl);

        }
        catch(InterruptedException e){
            /* TODO: Handle this exception, write log, inform user */
            e.printStackTrace();
        };

        client.authClient(loginTFiled.getText(), passTFiled.getText());

        curStage.setScene(mainScene);
        curStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

    }

    @FXML
    private void exit (ActionEvent event){
        System.exit(0);
    }


}
