package client;

import commands.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegController {
    @FXML
    public TextField loginField;
    @FXML
    public TextField nameField;
    @FXML
    public PasswordField passField;
    @FXML
    public Label statusLabel;

    @FXML
    public void regButtonClick(ActionEvent actionEvent) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        String result = Client.getInstance().sendMsg(String.format("%s;%s;%s;%s"
                , Command.REGISTRATION
                , loginField.getText()
                , passField.getText()
                , nameField.getText()
        )).toString();
        if (result.equals(Command.OK)) {
            Scene scene = ((Node)actionEvent.getSource()).getScene();
            Parent root = FXMLLoader.load(this.getClass().getResource("/main.fxml"));
            scene.setRoot(root);
        } else {
            statusLabel.setText(result);
        }
    }
}
