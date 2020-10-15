package ru.stk.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.stk.client.Model;

public class FXController {
    Stage curStage;
    Scene mainScene;
    FileChooser fileChooser = new FileChooser();
    Model model = new Model();

    @FXML
    private void login (ActionEvent event){
        //curStage.setScene(mainScene);
        Window curWindow = curStage.getOwner();
        fileChooser.showOpenDialog(curWindow);
    }

    @FXML
    private void exit (ActionEvent event){
        System.exit(0);
    }

    public void setCurStage(Stage stage){
        curStage = stage;
    }

    public void setMainScene(Scene scene){
        mainScene = scene;

    }

}
