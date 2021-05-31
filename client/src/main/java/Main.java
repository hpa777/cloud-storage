import controllers.Client;
import controllers.TableRow;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/loginpage.fxml"));
        primaryStage.setTitle("Cloud storage client");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        while (!Client.getInstance().isIsReady()) {

        }
        launch(args);
    }

}
