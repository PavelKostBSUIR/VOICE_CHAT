import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

public class Server extends Thread {
    HashMap<String, ClientHandler> users = new HashMap<>();
    private ServerSocket serverSocket;
    private boolean kill = false;
    Connection conn;

    Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            conn = getConnection();
            System.out.println("Connection to Store DB succesfull!");
            start();
        } catch (Exception ex) {
            System.out.println("Connection failed...");

        }
    }

    public void kill() {
        this.kill = true;
        //go to database and kill all the userHANDLERS
    }

    public Connection getConnection() throws SQLException, IOException {

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/database.properties"))) {
            props.load(in);
        }
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void run() {
        while (!kill) {
            try {
                Socket socket = serverSocket.accept();
                try {
                    System.out.println("Client connected!");
                    new ClientHandler(socket, this);
                } catch (IOException e) {
                    e.printStackTrace();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
