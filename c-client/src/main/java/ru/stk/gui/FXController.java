package ru.stk.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.core.util.JsonUtils;
import ru.stk.client.Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FXController {
    static private Stage curStage;
    static private Parent curRoot;
    private Scene mainScene;
    private FileChooser fileChooser = new FileChooser();
    private Client client = new Client();

    @FXML
    private TextField loginTFiled;
    @FXML
    private TextField passTFiled;

    //private Button uloadButton;

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
        loginTFiled.setText("Serge");
        passTFiled.setText("Serge7");


        try{
            client.connect();

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

    @FXML
    private void uploadFile (ActionEvent event) {

        Window curWindow = curStage.getOwner();
        fileChooser.setTitle("Выберите файл для загрузки");

        File file = fileChooser.showOpenDialog(curWindow);

        try{
            if (file != null) {
                System.out.println(file.getName());
                client.sendClientFile(Paths.get(file.getPath()));
            }
        }
        catch(IOException e){
            /* TODO: Handle this exception, write log, inform user */
            e.printStackTrace();
        };

    }


}
