package ru.stk.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.client.Client;

/**
 * Login form controller
 */
public class LoginFxCtl {

    private MainFxCtl mainFxCtl;    // controller of main form
    private Stage curStage;         // application stage
    private Scene mainScene;        // scene of main form
    private double mainWidth;       // width of main form
    private double mainHeight;      // height of main form

    private final FileChooser fileChooser = new FileChooser();
    private static final Logger logger = LogManager.getLogger(Client.class);

    @FXML
    private TextField loginTFiled;
    @FXML
    private TextField passTFiled;
    @FXML
    private Label lblMsg;

    public void setCurStage(Stage stage){
        curStage = stage;
    }

    public void setMainScene(Scene scene){
        mainScene = scene;

        // user login data. Used for application demo!
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

    /*
     * Login to server after "login" button is pressed
     */
    @FXML
    private void login (ActionEvent event){
        Client.connect(this, mainFxCtl);

        Client.authClient(loginTFiled.getText(), passTFiled.getText());

    }

    /*
     * Open main form if login successful
     */
    public void loginSuccessful(){

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Platform.runLater(() -> curStage.setScene(mainScene));
        curStage.setX((screenBounds.getWidth() - mainWidth) / 2);
        curStage.setY((screenBounds.getHeight() - mainHeight) / 2);
        curStage.setOnCloseRequest(event1 -> System.exit(0));
    }

    /*
     * Error message - login failed
     */
    public void loginFailed(String msg){
        Platform.runLater(() -> lblMsg.setText(msg));
    }

    /*
     * Error message - server not available
     */
    public void noServer(){
        Platform.runLater(() -> lblMsg.setText("Сервер недоступен, попробуйте подключиться позднее!"));
    }


    /*
     * Closes application if "cancel" button is pressed
     */
    @FXML
    private void cancel (ActionEvent event){
        System.exit(0);
    }
}