package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;


import java.net.URL;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class MainController implements Initializable {
    @FXML
    public TableView<TableRow> tableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Set<String> s = (Set<String>) Client.getInstance().sendMsg("ls");
            s.forEach(f -> {
                String[] info = f.split(";");
                tableView.getItems().add(new TableRow(info[0], info[1], info[2], info[3], info[4]));
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clickItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) //Checking double click
        {
            //System.out.println(tableView.getSelectionModel().getSelectedItem().getFileInfo());
           // System.out.println(tableView.getSelectionModel().getSelectedItem().getFileName());
        }
    }

    public void btnClick(ActionEvent actionEvent) {




    }
}
