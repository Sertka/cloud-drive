package ru.stk.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.client.Client;
import ru.stk.client.UserFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Main form controller
 */

public class MainFxCtl {
    private Stage curStage;         // application stage
    private  Scene mainScene;       // scene of main form
    private UserFile selectedFile;  //selected file in a list
    private final String FILE_SEND_ERROR = "Невозможно отправить файл";
    private final String FILE_RENAME_ERROR = "Возникла ошибка при переименовании файла, попробуйте еще раз";
    private final String FILE_DELETE_ERROR = "Возникла ошибка при удалении файла";
    private final String FILE_DOWNLOAD_INFO = "Файл сохранен на локальном диске";

    private final FileChooser fileChooser = new FileChooser();
    private static final Logger logger = LogManager.getLogger(Client.class);

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

        curStage.setOnCloseRequest(event -> System.exit(0));
    }

    public void setMainScene(Scene scene){
        mainScene = scene;
    }

    /*
     * Fills in table with user's files
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

        // fill in "total" counters
        Platform.runLater(() -> lblTotalSize.setText(Client.getFolderSize()));
        Platform.runLater(() -> lblTotalFiles.setText(Client.getFolderCount()));

    }

    /*
     * If user has selected a file
     */
    @FXML
    private void rowChanged(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        selectedFile = tbvFiles.getItems().get(row);
        enableButtons();
    }

    /*
     * Button "download" is pressed
     */
    @FXML
    private void downloadFile(){
        Client.downloadFile(selectedFile.getName());
    }

    /*
     * Button "rename" is pressed
     */
    @FXML
    private void renameFile(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        String fileName = tbvFiles.getItems().get(row).getName();
        Client.renameFile(fileName);
    }

    /*
     * Button "delete" is pressed
     */
    @FXML
    private void deleteFile(){
        int row;
        row = tbvFiles.getFocusModel().getFocusedCell().getRow();
        selectedFile = tbvFiles.getItems().get(row);
        Client.deleteFile(selectedFile.getName());
    }

    /*
     * Button "upload" is pressed
     */
    @FXML
    private void uploadFile (ActionEvent event) {

        Window curWindow = curStage.getOwner();
        fileChooser.setTitle("Выберите файл для загрузки");

        File file = fileChooser.showOpenDialog(curWindow);

        try{
            if (file != null) {
                Client.sendClientFile(Paths.get(file.getPath()));
            }
        }
        catch(IOException e){
            logger.error("File sending failure - " + e.getMessage());
            Platform.runLater(() -> MsgBox.showErrorMsg(mainScene, FILE_SEND_ERROR));
        };
    }

    public void renameFailed(){
        Platform.runLater(() -> MsgBox.showErrorMsg(mainScene, FILE_RENAME_ERROR));
    }

    public void fileDownloaded(){
        Platform.runLater(() -> MsgBox.showInfoMsg(mainScene, FILE_DOWNLOAD_INFO));
    }

    public void deleteFailed(){
        Platform.runLater(() -> MsgBox.showErrorMsg(mainScene, FILE_DELETE_ERROR));
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
}
