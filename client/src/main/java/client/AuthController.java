

package client;

import java.io.IOException;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class AuthController
{
    public void continueClick(final ActionEvent actionEvent) throws IOException {
        final Scene scene = ((Node)actionEvent.getSource()).getScene();
        final Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("/main.fxml"));
        scene.setRoot(root);
    }
}
