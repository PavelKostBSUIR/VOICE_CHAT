import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthorizationGUIController {
    private Stage stage;
    private Client client;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Client client) {
        this.client = client;
        client.setAuthorizationGUIController(this);
    }

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextArea messagesArea;

    @FXML
    void createAcc(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("CreateAcc.fxml"));
        AnchorPane rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);
        stage.setScene(scene);
        CreateNewAccGUIController createNewAccGUIController = loader.getController();
        createNewAccGUIController.setStage(stage);
        createNewAccGUIController.setClient(client);
        client.setAuthorizationGUIController(null);
    }

    @FXML
    void logIn(ActionEvent event) {
        if (loginField.getText().equals("") || passwordField.getText().equals("")) {
            messagesArea.setText("Поле логина или пароля не заполнено.");
        } else {
            client.handleAuthorizeRequest(loginField.getText(), passwordField.getText());
        }

    }

    public void handleAuthorizationResponse(Message response) {
        if (response.status == Status.ERROR_USER) {
            messagesArea.setText("Пользователя с таким логином не существует.");
        } else if (response.status == Status.ERROR_PASS) {
            messagesArea.setText("Неправильный пароль.");
        } else if (response.status == Status.OK) {
            messagesArea.setText("Вы авторизованы.");
            Platform.runLater(() -> {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Client.class.getResource("Client.fxml"));
                AnchorPane rootLayout = null;
                try {
                    rootLayout = loader.load();
                    Scene scene = new Scene(rootLayout);
                    stage.setScene(scene);
                    ClientGUIController clientGUIController = loader.getController();
                    clientGUIController.setStage(stage);
                    clientGUIController.setClient(client);
                    clientGUIController.initTable();
                    clientGUIController.setNickLabelText(response.params.get("login"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}