package ru.stk.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.stk.client.Client;
import ru.stk.client.UserFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class MainFxCtl {
    private final FileChooser fileChooser = new FileChooser();
    private Stage curStage;
    private UserFile selectedFile;


    @FXML
    private Button renameButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Label lblTotalFiles;
    @FXML
    private Label lblTotalSize;


    @FXML
    private TableView<UserFile> tbvFiles;
    @FXML
    private TableColumn<UserFile, String> clnFileName;
    @FXML
    private TableColumn<UserFile, String> clnFileSize;
    @FXML
    private TableColumn<UserFile, String> clnFileDate;


    public void setCurStage(Stage stage){
        curStage = stage;
        disableButtons();
    }

/*    public void setMainScene(Scene scene){
        mainScene = scene;
    }*/


    /**
     * Fills in table with user files
     * @param list
     */

    public void fillFileTable (ObservableList<UserFile> list){

        if (list == null){
            tbvFiles.setItems(null);
            tbvFiles.refresh();
            Platform.runLater(() -> lblTotalSize.setText(""));
            Platform.runLater(() -> lblTotalFiles.setText(""));
            return;
        }

        clnFileName.setCellValueFactory(new PropertyValueFactory<UserFile, String>("name"));
        clnFileSize.setCellValueFactory(new PropertyValueFactory<UserFile, String>("size"));
        clnFileDate.setCellValueFactory(new PropertyValueFactory<UserFile, String>("date"));
        clnFileSize.setStyle( "-fx-alignment: CENTER-RIGHT;");
        clnFileDate.setStyle( "-fx-alignment: CENTER-RIGHT;");
        tbvFiles.setItems(list);

        Platform.runLater(() -> lblTotalSize.setText(Client.getFolderSize()));
        Platform.runLater(() -> lblTotalFiles.setText(Client.getFolderCount()));

    }

    @FXML
    private void rowChanged(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        selectedFile = tbvFiles.getItems().get(row);
        enableButtons();
    }

    @FXML
    private void downloadFile(){
        Client.downloadFile(selectedFile.getName());
    }

    @FXML
    private void renameFile(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        String fileName = tbvFiles.getItems().get(row).getName();
        Client.renameFile(fileName);
    }

    @FXML
    private void deleteFile(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        selectedFile = tbvFiles.getItems().get(row);
        Client.deleteFile(selectedFile.getName());
    }
    @FXML
    private void uploadFile (ActionEvent event) {

        Window curWindow = curStage.getOwner();
        fileChooser.setTitle("Выберите файл для загрузки");

        File file = fileChooser.showOpenDialog(curWindow);


        try{
            if (file != null) {
                System.out.println(file.getName());
                Client.sendClientFile(Paths.get(file.getPath()));
            }
        }
        catch(IOException e){
            /* TODO: Handle this exception, write log, inform user */
            e.printStackTrace();
        };
    }

    public void disableButtons() {
        renameButton.setDisable(true);
        deleteButton.setDisable(true);
        downloadButton.setDisable(true);
    }

    public void enableButtons() {
        renameButton.setDisable(false);
        deleteButton.setDisable(false);
        downloadButton.setDisable(false);
    }


    @FXML
    private void exit (ActionEvent event){
        System.exit(0);
    }

}
