package ru.stk.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;
import java.io.InputStream;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //load fxml and create login scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Scene loginScene = new Scene(root, 400, 400);
        //create and initialize controller
        FXController controller = loader.getController();
        controller.setCurStage(primaryStage);
        //create main scene
        FXMLLoader loaderMain = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent rootMain = loaderMain.load();
        Scene mainScene = new Scene(rootMain, 400, 400);
        //set main scene in controller
        controller.setMainScene(mainScene);
        //set app title
        primaryStage.setTitle("Сетевое хранилище cloud drive");
        //set up icon for the form header
        InputStream iconStream = getClass().getResourceAsStream("/disk.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        //set scene and show window
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

}
