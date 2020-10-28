package ru.stk.gui;

import javafx.scene.Scene;
import javafx.scene.control.Alert;

/**
 * Class contains static methods with messages to user
 */
public class MsgBox {

    public static void showErrorMsg(Scene scene, String message){
                try {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("cloud.drive");
                    alert.setHeaderText("Возникла ошибка");
                    alert.setContentText(message);

                    alert.initOwner(scene.getWindow());
                    alert.showAndWait();

                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
    }

    public static void showInfoMsg(Scene scene, String message){
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("cloud.drive");
            alert.setHeaderText("Информация");
            alert.setContentText(message);

            alert.initOwner(scene.getWindow());
            alert.showAndWait();

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
