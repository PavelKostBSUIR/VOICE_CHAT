import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateNewAccGUIController {
    private Stage stage;
    private Client client;

    public void setClient(Client client) {
        this.client = client;
        client.setCreateNewAccGUIController(this);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextArea messagesArea;

    @FXML
    void back(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("Authorization.fxml"));
        AnchorPane rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);
        stage.setScene(scene);
        AuthorizationGUIController authorizationGUIController = loader.getController();
        authorizationGUIController.setStage(stage);
        authorizationGUIController.setClient(client);
        client.setCreateNewAccGUIController(null);
    }

    @FXML
    void create(ActionEvent event) {
        if (loginField.getText().equals("") || passwordField.getText().equals("")) {
            messagesArea.setText("Поле логина или пароля не заполнено.");
        } else {
            client.handleCreateAccountRequest(loginField.getText(), passwordField.getText());
        }
    }

    public void handleCreateAccountResponse(Message response) {
        if (response.status == Status.ERROR) {
            messagesArea.setText("Аккаунт с таким логином существует");
        } else if (response.status == Status.OK) {
            messagesArea.setText("Аккаунт создан");
        }
    }
}
