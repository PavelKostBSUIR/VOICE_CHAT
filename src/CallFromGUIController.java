import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class CallFromGUIController {
    private Stage stage;
    private Client client;
    private Message message;
    private UpdateConnectionHandler updateConnectionHandler;

    public void setUpdateConnectionHandler(UpdateConnectionHandler updateConnectionHandler) {
        this.updateConnectionHandler = updateConnectionHandler;
    }

    public void setMessage(Message message) {
        this.message = message;
        callerLabel.setText(message.params.get("login"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(we -> {
            reject(null);
        });
    }

    public void setClient(Client client) {
        this.client = client;
        client.setCallFromGUIController(this);
    }

    @FXML
    private Label callerLabel;

    @FXML
    void accept(ActionEvent event) {
        message.method = Method.INP_CALL_RESP;
        message.status = Status.OK;
        client.handleInputCallResponse(message);
        client.setCallFromGUIController(null);
        stage.close();
        try {
            if (updateConnectionHandler != null)
                updateConnectionHandler.kill();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("CallTo.fxml"));
            AnchorPane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.stage);
            stage.show();
            CallToGUIController callToGUIController = loader.getController();
            callToGUIController.setStage(stage);
            callToGUIController.setClient(client);
            callToGUIController.setStatusLabelText("Соединен");
            callToGUIController.setCallerLogin(message.params.get("login"));
            callToGUIController.setInterlocutorLabelText(callerLabel.getText());
            UpdateConnectionHandler connectionHandler = new UpdateConnectionHandler(message.params.get("login"), client, callToGUIController);
            callToGUIController.setUpdateConnectionHandler(connectionHandler);

            connectionHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void reject(ActionEvent event) {
        if (updateConnectionHandler != null)
            updateConnectionHandler.kill();
        stage.close();
        message.method = Method.INP_CALL_RESP;
        message.status = Status.REJECTED;
        client.setCallFromGUIController(null);
        client.handleInputCallResponse(message);

    }

    public void cancel() {
        stage.close();
    }
}
