import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.SocketException;
import java.util.Scanner;

public class ClientMain extends Application {
    public static void main(String[] args) throws SocketException {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("ConnectToServer.fxml"));
        AnchorPane rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);
        stage.setResizable(false);
        stage.setScene(scene);
        ConnectToServerController connectToServerController = loader.getController();
        connectToServerController.setStage(stage);
        stage.show();

    }
}
