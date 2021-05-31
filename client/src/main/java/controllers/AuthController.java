package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class AuthController {
    public void continueClick(ActionEvent actionEvent) throws IOException {
        Scene scene = ((Node)actionEvent.getSource()).getScene();
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        scene.setRoot(root);
    }
}
