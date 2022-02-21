import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.WriteAbortedException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientGUIController {
    private boolean updated = false;
    User user;
    private Stage stage;
    private Client client;
    private ObservableList<User> users = FXCollections.observableArrayList();
    private CallToGUIController callToGUIController;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Client client) {
        this.client = client;
        client.setClientGUIController(this);
    }

    public void initTable() {
        userTable.setItems(users);
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statement"));
        TableView.TableViewSelectionModel<User> selectionModel = userTable.getSelectionModel();
        selectionModel.selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if (newVal != null) {
                user = newVal;
            }
        });
    }

    public void setNickLabelText(String text) {
        this.nickLabel.setText(text);
    }

    public void setMessageTextAreaText(String text) {
        messageTextArea.clear();
        messageTextArea.setText(text);
    }

    @FXML
    private Label nickLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> loginColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    void call(ActionEvent event) throws SocketException, UnknownHostException {
        if (user != null && user.statement.equals("online")) {
            client.handleCallRequest(user.login);
            try {

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
                this.callToGUIController = callToGUIController;
                callToGUIController.setStage(stage);
                callToGUIController.setStatusLabelText("Соединение");
                callToGUIController.setClient(client);
                callToGUIController.setCallerLogin(user.login);
                callToGUIController.setInterlocutorLabelText(user.login);
                UpdateConnectionHandler connectionHandler = new UpdateConnectionHandler(user.login, client, callToGUIController);
                callToGUIController.setUpdateConnectionHandler(connectionHandler);
                connectionHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void logOut(ActionEvent event) {
        client.handleLogOutRequest();
    }

    @FXML
    void updateUsers(ActionEvent event) {
        client.handleGetUsersRequest();
    }

    public void handleCallToResponse(Message message) {
        Platform.runLater(() -> {

            if (message.status == Status.ERROR_USER) {
            } else if (message.status == Status.OK) {
                if (callToGUIController != null) {
                    callToGUIController.setStatusLabelText("Соединен");
                }
                String ip = message.params.get("ip");
                int port = Integer.parseInt(message.params.get("port"));

            }
        });
    }

    public void handleLogOutResponse(Message message) {
        Platform.runLater(() -> {
            if (message.status == Status.OK) ;
            {
                try {
                    client.setClientGUIController(null);
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Client.class.getResource("Authorization.fxml"));
                    AnchorPane rootLayout = loader.load();
                    Scene scene = new Scene(rootLayout);
                    stage.setScene(scene);
                    AuthorizationGUIController authorizationGUIController = loader.getController();
                    authorizationGUIController.setStage(stage);
                    authorizationGUIController.setClient(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void handleUpdateUsersResponse(Message message) {
        HashMap<String, String> usersWithStates = new HashMap<>();
        HashMap<String, String> users = message.params;
        int i = 0;
        ArrayList<User> list = new ArrayList<>();
        while (!users.isEmpty()) {
            User user = new User(users.remove("login_" + i), users.remove("statement_" + i));
            usersWithStates.put(user.login, user.statement);
            list.add(user);
            i++;
        }
        client.setUsersWithStates(usersWithStates);
        this.users.clear();
        this.users.addAll(list);
        notify();

    }

    public synchronized void handleInputCallRequest(Message message) {
        Object obj = this;
        Platform.runLater(() -> {
            if (message.status == Status.OK) ;
            {

                client.handleGetUsersRequest();
                try {
                    synchronized (obj) {
                        obj.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Client.class.getResource("CallFrom.fxml"));
                    AnchorPane rootLayout = loader.load();
                    Scene scene = new Scene(rootLayout);
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(this.stage);
                    stage.show();
                    CallFromGUIController callFromGUIController = loader.getController();
                    callFromGUIController.setStage(stage);
                    callFromGUIController.setClient(client);
                    callFromGUIController.setMessage(message);
                    UpdateConnectionHandler connectionHandler = new UpdateConnectionHandler(message.params.get("login"), client, callFromGUIController);
                    callFromGUIController.setUpdateConnectionHandler(connectionHandler);
                    connectionHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
