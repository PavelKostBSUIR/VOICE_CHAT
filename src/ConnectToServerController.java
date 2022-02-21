import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLOutput;

public class ConnectToServerController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private TextArea messagesArea;

    @FXML
    void connect(ActionEvent event) {

        if (ipField.getText().equals("") || portField.getText().equals("")) {
            messagesArea.setText("IP или порт не введены");
        } else if (!Utils.isInt(portField.getText())) {
            messagesArea.setText("Порт должен быть целым чилом.");

        } else {
            try {
                Client client = new Client(ipField.getText(), Integer.parseInt(portField.getText()));
                messagesArea.setText("Вы подключились");
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Client.class.getResource("Authorization.fxml"));
                AnchorPane rootLayout = loader.load();
                Scene scene = new Scene(rootLayout);
                stage.setScene(scene);
                AuthorizationGUIController authorizationGUIController = loader.getController();
                authorizationGUIController.setStage(stage);
                authorizationGUIController.setClient(client);
                stage.setOnCloseRequest(we -> {
                    System.out.println("Stage is closing");
                    client.kill();
                });
            } catch (IOException e) {
                e.printStackTrace();
                messagesArea.setText("Сервер недоступен");
            }
        }
    }

}
