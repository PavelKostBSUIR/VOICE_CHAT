import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import java.net.UnknownHostException;

public class CallToGUIController {
    private Client client;
    private String callerLogin;
    private Stage stage;
    private boolean dynOn = true;
    private boolean micOn = true;
    private UpdateConnectionHandler updateConnectionHandler;

    public void setUpdateConnectionHandler(UpdateConnectionHandler updateConnectionHandler) {
        this.updateConnectionHandler = updateConnectionHandler;
    }

    public void setCallerLogin(String callerLogin) {
        this.callerLogin = callerLogin;
    }

    public void setClient(Client client) {
        this.client = client;
        client.setCallToGUIController(this);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(we -> {
            cancel();
            updateConnectionHandler.kill();
        });
    }

    public void setInterlocutorLabelText(String interlocutorLabelText) {
        this.interlocutorLabel.setText(interlocutorLabelText);
    }

    public void setStatusLabelText(String text) {
        statusLabel.setText(text);
    }

    @FXML
    private Label statusLabel;
    @FXML
    private Label interlocutorLabel;
    @FXML
    private Button dynButton;

    @FXML
    private Button micButton;

    @FXML
    void cancel(ActionEvent event) {
        cancel();
    }

    @FXML
    void changeDyn(ActionEvent event) {
        if (dynOn) {
            client.voiceSession.stopDyn();
            dynOn = false;
            dynButton.setText("Включить динамик");

        } else {
            client.voiceSession.startDyn();
            dynButton.setText("Выключить динамик");
            dynOn = true;
        }
    }

    @FXML
    void changeMic(ActionEvent event) throws LineUnavailableException, UnknownHostException {
        if (micOn) {
            client.voiceSession.offMic();
            micOn = false;
            micButton.setText("Включить микрофон");

        } else {
            client.voiceSession.startMic();
            micButton.setText("Выключить микрофон");
            micOn = true;
        }
    }

    public void handleCallEndingResponse(Message message) {
        Platform.runLater(() -> {
            client.getClientGUIController().setMessageTextAreaText("Собеседник завершил вызов");
            stage.close();
            if (updateConnectionHandler != null) {
                updateConnectionHandler.kill();
            }
            if (client.voiceSession != null) {
                client.voiceSession.kill();
            }
            client.setCallToGUIController(null);
            System.out.println("вызов завершен");
        });

    }

    public void cancel() {
        Platform.runLater(() -> {
            client.handleCallEndingRequest(callerLogin);
            stage.close();
            if (client.voiceSession != null) {
                client.voiceSession.kill();
            }
            client.setCallToGUIController(null);
            if (updateConnectionHandler != null) {
                updateConnectionHandler.kill();
            }
        });
    }
}
