
package client;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.application.Application;

public class Main extends Application
{
    public void start(final Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(this.getClass().getResource("/loginpage.fxml"));
        primaryStage.setTitle("Cloud storage client");
        primaryStage.setScene(new Scene(root, 800.0, 500.0));
        primaryStage.show();
    }
    
    public static void main(final String[] args) {
        while (!Client.getInstance().isIsReady()) {}
        launch(args);
    }
}
