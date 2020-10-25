package ru.stk.gui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.*;
import ru.stk.client.Client;
import ru.stk.client.UserFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LoginFxCtl {


    private MainFxCtl mainFxCtl;
    private Stage curStage;
    private Scene mainScene;
    private double mainWidth;
    private double mainHeight;
    private final FileChooser fileChooser = new FileChooser();
    private Client client;


    @FXML
    private TextField loginTFiled;
    @FXML
    private TextField passTFiled;

    public void setClient(Client client){

        this.client = client;
    }

    public void setCurStage(Stage stage){
        curStage = stage;
    }

    public void setMainScene(Scene scene){
        mainScene = scene;
        loginTFiled.setText("Serge");
        passTFiled.setText("Serge108");
    }

    public void setMainFormWidth(double width){
        mainWidth = width;
    }

    public void setMainFormHeight(double height){
        mainHeight = height;
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

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        curStage.setX((screenBounds.getWidth() - mainWidth) / 2);
        curStage.setY((screenBounds.getHeight() - mainHeight) / 2);

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
