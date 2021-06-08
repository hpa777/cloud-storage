

package client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class AuthController
{
    @FXML
    public void continueClick(final ActionEvent actionEvent) throws IOException {
        Scene scene = ((Node)actionEvent.getSource()).getScene();
        Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("/main.fxml"));
        scene.setRoot(root);
    }
}
