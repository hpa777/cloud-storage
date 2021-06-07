package client;

import commands.Command;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.event.ActionEvent;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import javafx.scene.input.MouseEvent;
import javafx.application.Platform;
import java.util.ResourceBundle;
import java.net.URL;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.fxml.Initializable;

public class MainController implements Initializable
{
    @FXML
    public TableView<TableRow> tableView;
    @FXML
    public Button removeButton;
    @FXML
    public Button copyButton;
    @FXML
    public Button pasteButton;
    @FXML
    public Button downloadButton;
    @FXML
    public Button uploadButton;
    @FXML
    public Button renameButton;

    private Stage stage;
    
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            pasteButton.setDisable(true);
            stage = (Stage) tableView.getScene().getWindow();
            stage.setOnCloseRequest(event -> Client.getInstance().stopClient());
        });
        refreshTableView();
    }
    
    @FXML
    public void clickItem(MouseEvent mouseEvent) {
        TableRow tableRow = tableView.getSelectionModel().getSelectedItem();
        if (tableRow == null) {
            return;
        }
        setButtonsDisable(false);
        boolean isDir = !tableRow.getIsDir().isEmpty();
        downloadButton.setDisable(isDir);
        if (mouseEvent.getClickCount() == 2 && isDir) {
            changeDir(tableRow.getFileName());
        }
    }
    
    private Object sendCommand(Object command) {
        Object result = null;
        try {
            result = Client.getInstance().sendMsg(command);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e2) {
            e2.printStackTrace();
        }
        catch (TimeoutException e3) {
            e3.printStackTrace();
        }
        return result;
    }
    
    private void refreshTableView() {
        Platform.runLater(() -> {
            setButtonsDisable(true);
            downloadButton.setDisable(true);
            tableView.getItems().clear();
            Set<String> s = (Set<String>)sendCommand(Command.LIST_FILES);
            s.forEach(f -> {
                String[] info = f.split(Command.DELIMITER);
                TableRow t = new TableRow(info[0], info[1], info[2], info[3], info[4]);
                this.tableView.getItems().add(t);
            });
        });
    }
    
    private void setButtonsDisable(boolean disable) {
        removeButton.setDisable(disable);
        renameButton.setDisable(disable);
        copyButton.setDisable(disable);
    }
    
    @FXML
    public void goToUpDirButtonClick(ActionEvent actionEvent)  {
        this.changeDir(Command.GO_UP_DIR);
    }
    
    @FXML
    public void goToRootDirButtonClick(ActionEvent actionEvent)  {

        this.changeDir(Command.GO_ROOT_DIR);
    }
    
    @FXML
    public void makeDirButtonClick(ActionEvent actionEvent) {
        this.showMkdirDialog();
    }
    
    @FXML
    public void removeButtonClick(ActionEvent actionEvent) {
        showRemoveDialog();
    }
    
    @FXML
    public void renameButtonClick(ActionEvent actionEvent) {
        showRenameDialog();
    }
    
    @FXML
    public void copyButtonClick(ActionEvent actionEvent) {
        TableRow tableRow = tableView.getSelectionModel().getSelectedItem();
        if (tableRow == null) {
            return;
        }
        sendCommand(Command.COPY + tableRow.getFileName());
        pasteButton.setDisable(false);
    }
    
    @FXML
    public void pasteButtonClick(ActionEvent actionEvent) {
        sendCommand(Command.PASTE);
        pasteButton.setDisable(true);
        refreshTableView();
    }
    
    @FXML
    public void downloadButtonClick(ActionEvent actionEvent) {
        download(tableView.getSelectionModel().getSelectedItem().getFileName());
    }
    
    @FXML
    public void uploadButtonClick(ActionEvent actionEvent) {
        upload();
    }

    public static final String FILE_ALREADY_EXISTS = "File already exists";
    
    private void upload() {
        File file = this.showFileChooser("Upload", "");
        if (file != null) {
            String response = this.sendCommand(Command.UPLOAD + file.getName()).toString();
            if (response.equals(Command.WAIT)) {
                try {
                    sendCommand(Files.readAllBytes(file.toPath()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                refreshTableView();
            }
            else if (response.equals(Command.ALREADY_EXISTS)) {
                this.showAlert(FILE_ALREADY_EXISTS);
            }
        }
    }
    
    private void download(String fileName) {
        File file = this.showFileChooser("Download", fileName);
        if (Files.exists(file.toPath())) {
            showAlert(FILE_ALREADY_EXISTS);
        } else if (file != null) {
            byte[] data = (byte[])sendCommand(Command.DOWNLOAD + fileName);
            try {
                Files.write(file.toPath(), data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private File showFileChooser(String title, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (!defaultFileName.isEmpty()) {
            fileChooser.setInitialFileName(defaultFileName);
        }
        return defaultFileName.isEmpty() ? fileChooser.showOpenDialog(stage) : fileChooser.showSaveDialog(stage);
    }
    
    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(text);
        alert.setContentText("");
        alert.showAndWait();
    }
    
    private void changeDir(String command) {
        sendCommand(Command.CHANGE_DIR + command);
        refreshTableView();
    }
    
    private void showMkdirDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Make directory dialog");
        dialog.setHeaderText("Please enter directory name");
        dialog.setContentText("");
        dialog.initOwner(stage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(dir -> {
            sendCommand(Command.MAKE_DIR + dir);
            refreshTableView();
        });
    }
    
    private void showRemoveDialog() {
        TableRow tableRow = tableView.getSelectionModel().getSelectedItem();
        if (tableRow == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove dialog");
        alert.setHeaderText("Are you sure?");
        alert.setContentText(String.format("Remove `%s`", tableRow.getFileName()));
        alert.initOwner(stage);
        alert.initStyle(StageStyle.UTILITY);
        alert.initModality(Modality.WINDOW_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            sendCommand(Command.REMOVE + tableRow.getFileName());
            refreshTableView();
        }
    }
    
    private void showRenameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename dialog");
        dialog.setHeaderText("Please enter new name");
        dialog.setContentText("");
        dialog.initOwner(stage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newFileName -> {
            String response = sendCommand(String.format("%s %s %s"
                    , Command.RENAME
                    , tableView.getSelectionModel().getSelectedItem().getFileName()
                    , newFileName)).toString();
            if (response.equals(Command.ALREADY_EXISTS)) {
                showAlert(FILE_ALREADY_EXISTS);
            }
            else if (response.equals(Command.OK)) {
                refreshTableView();
            }
        });
    }
}