package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ConnectController {

    @FXML
    public TextField ipField;
    @FXML
    public TextField portField;
    @FXML
    public Label statusLabel;

    @FXML
    public void connectClick(ActionEvent actionEvent) throws InterruptedException, IOException {
        Client client = Client.getInstance();
        client.setHost(ipField.getText());
        client.setPort(Integer.parseInt(portField.getText(), 10));
        synchronized (client) {
            client.tryConnect();
            client.wait();
        }
        if (client.isReady()) {
            Scene scene = ((Node)actionEvent.getSource()).getScene();
            Parent root = FXMLLoader.load(this.getClass().getResource("/loginpage.fxml"));
            scene.setRoot(root);
        } else {
            statusLabel.setText("Connection error!");
        }

    }
}
