package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
    @FXML
    public TextField searchField;
    @FXML
    public Button searchButton;
    @FXML
    public Label breadCrumbs;

    private Stage stage;
    
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            pasteButton.setDisable(true);
            stage = (Stage) tableView.getScene().getWindow();
            stage.setOnCloseRequest(event -> Client.getInstance().stopClient());
        });
        refreshTableView("");
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

    /**
     * Отправка объекта на сервер и получение ответа
     * @param command
     * @return
     */
    private Object sendCommand(Object command) {
        Object result = null;
        try {
            result = Client.getInstance().sendMsg(command);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Обновление содержимого TableView и состояния кнопок.
     * Если задан параметр selectName, выделяется строка содержащая этот параметр в столбце fileName.
     * @param selectName
     */
    private void refreshTableView(String selectName) {
        Platform.runLater(() -> {
            setButtonsDisable(true);
            downloadButton.setDisable(true);
            breadCrumbs.setText(sendCommand(Command.GET_CURRENT_PATH).toString());
            tableView.getItems().clear();
            Set<String> s = (Set<String>)sendCommand(Command.LIST_FILES);
            s.forEach(f -> {
                String[] info = f.split(Command.DELIMITER);
                TableRow t = new TableRow(info[0], info[1], info[2], info[3], info[4]);
                tableView.getItems().add(t);
                if (!selectName.isEmpty() && info[0].toLowerCase(Locale.ROOT).contains(selectName.toLowerCase(Locale.ROOT))) {
                    tableView.getSelectionModel().select(t);
                }
            });
        });
    }

    /**
     * Обновление состояния кнопок
     * @param disable
     */
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
        refreshTableView("");
    }
    
    @FXML
    public void downloadButtonClick(ActionEvent actionEvent) {
        download(tableView.getSelectionModel().getSelectedItem().getFileName());
    }
    
    @FXML
    public void uploadButtonClick(ActionEvent actionEvent) {
        upload();
    }

    @FXML
    public void searchButtonClick(ActionEvent actionEvent) {
        search();
    }

    @FXML
    public void disconnectButtonClick(ActionEvent actionEvent) throws IOException {
        Client.getInstance().stopClient();
        Parent root = FXMLLoader.load(this.getClass().getResource("/connectpage.fxml"));
        stage.getScene().setRoot(root);
    }

    private void search() {
        String search = searchField.getText();
        if (search.isEmpty()) {
            return;
        }
        String response = (String) sendCommand(Command.FIND + search);
        if (response.equals(Command.OK)) {
            refreshTableView(search);
        }
    }

    public static final String FILE_ALREADY_EXISTS = "File already exists";
    
    private void upload() {
        File file = showFileChooser("Upload", "");
        if (file != null) {
            String response = sendCommand(Command.UPLOAD + file.getName()).toString();
            if (response.equals(Command.WAIT)) {
                try {
                    sendCommand(Files.readAllBytes(file.toPath()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                refreshTableView("");
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
        refreshTableView("");
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
            refreshTableView("");
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
            refreshTableView("");
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
                refreshTableView("");
            }
        });
    }


}
