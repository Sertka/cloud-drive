package ru.stk.gui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private final Client client = new Client();
    private Stage curStage;

    @FXML
    private Button newFolderButton;
    @FXML
    private TableView<UserFile> tbvFiles;
    @FXML
    private TableColumn<UserFile, String> clnFileName;
    @FXML
    private TableColumn<UserFile, String> clnFileSize;
    @FXML
    private TableColumn<UserFile, String> clnFileDate;

    //private Button uloadButton;

    public void setCurStage(Stage stage){
        curStage = stage;
    }




    /**
     * Fills in table with user files
     * @param list
     */
    public void fillFileTable (ObservableList<UserFile> list){

        // столбец для вывода имени
        System.out.println(tbvFiles.getColumns().toString());


        clnFileName.setCellValueFactory(new PropertyValueFactory<UserFile, String>("name"));
        clnFileSize.setCellValueFactory(new PropertyValueFactory<UserFile, String>("size"));
        clnFileDate.setCellValueFactory(new PropertyValueFactory<UserFile, String>("date"));
        clnFileSize.setStyle( "-fx-alignment: CENTER-RIGHT;");
        clnFileDate.setStyle( "-fx-alignment: CENTER-RIGHT;");
        tbvFiles.setItems(list);
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

        newFolderButton.getText();

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

    @FXML
    public void initialize() {
        //loginTFiled.setText("Serge2");
    }

}
