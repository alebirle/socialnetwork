package controller;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MessageAlert {
    static void showMessage(Stage owner, Alert.AlertType type, String header, String text){
        Alert message=new Alert(type);
        DialogPane dialogPane = message.getDialogPane();
        dialogPane.getStylesheets().add("/css/Style.css");
        message.setHeaderText(header);
        message.setContentText(text);
        message.initOwner(owner);
        message.showAndWait();
    }

    static void showErrorMessage(Stage owner, String text){
        Alert message=new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = message.getDialogPane();
        dialogPane.getStylesheets().add("/css/Style.css");
        message.initOwner(owner);
        message.setTitle("Error message");
        message.setContentText(text);
        message.showAndWait();
    }
}
