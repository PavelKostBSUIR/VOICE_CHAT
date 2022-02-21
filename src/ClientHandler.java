import java.io.*;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ClientHandler {

    private Socket socket;
    Queue<Message> messages = new ArrayDeque<>();
    private OutputHandler outputHandler;
    private InputHandler inputHandler;
    private Server server;
    private String login;
    private boolean busy = false;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        outputHandler = new OutputHandler(out, messages);
        inputHandler = new InputHandler(in, new ServInpHandlerMethod(this));
        //ew ConnectionChecker(socket, this);
    }

    public void kill(Message message) {
        inputHandler.kill();
        outputHandler.kill();
        outputHandler.wakeUp();
        if (login != null) {
            deleteUser();
        }
    }

    public void logOut(Message message) {
        if (login != null) {
            deleteUser();
            messages.add(new Message(Method.LOG_OUT, Status.OK, null));
        } else {
            messages.add(new Message(Method.LOG_OUT, Status.PERM_DEN, null));
        }
        outputHandler.wakeUp();
    }

    public void authorize(Message message) throws SQLException {
        synchronized (server.conn) {
            String login = message.params.get("login");
            String password = message.params.get("password");
            String sql = "SELECT * FROM users WHERE login=?";
            PreparedStatement preparedStatement = server.conn.prepareStatement(sql);
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                messages.add(new Message(Method.AUTHORIZE, Status.ERROR_USER, null));
            } else if (!resultSet.getString(2).equals(password)) {

                messages.add(new Message(Method.AUTHORIZE, Status.ERROR_PASS, null));
            } else if (!resultSet.getString(1).equals(login)) {
                messages.add(new Message(Method.AUTHORIZE, Status.ERROR_USER, null));
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", login);
                messages.add(new Message(Method.AUTHORIZE, Status.OK, params));
                this.login = login;
                server.users.put(login, this);
            }
            outputHandler.wakeUp();
        }
    }

    public void call(Message message) {
        if (login != null) {

            String login = message.params.get("login");
            String ip = message.params.get("ip");
            String port = message.params.get("port");
            ClientHandler clientHandler = server.users.get(login);
            if (clientHandler == null) {
                busy = false;
                messages.add(new Message(Method.CALL, Status.ERROR_USER, null));
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", this.login);
                params.put("port", port);
                params.put("ip", ip);
                //may  be synchronized(don't works with synchronization)
                clientHandler.messages.add(new Message(Method.INP_CALL, Status.OK, params));
                clientHandler.outputHandler.wakeUp();
                busy = true;
                clientHandler.busy = true;

            }
        } else {
            messages.add(new Message(Method.CALL, Status.PERM_DEN, null));
            outputHandler.wakeUp();
        }

    }

    public void getUsers(Message message) throws SQLException {
        if (login != null) {
            HashMap<String, String> users = new HashMap<>();
            Statement statement = server.conn.createStatement();
            String sql = "SELECT * FROM users";
            ResultSet resultSet = statement.executeQuery(sql);
            int i = 0;
            while (resultSet.next()) {
                String login = resultSet.getString("login");
                String user_statement = "offline";
                ClientHandler clientHandler = server.users.get(login);
                if (clientHandler != null) {
                    if (clientHandler.busy) {
                        user_statement = "busy";
                    } else {
                        user_statement = "online";
                    }
                }
                if (!login.equals(this.login)) {
                    users.put("login_" + i, login);
                    users.put("statement_" + i, user_statement);
                    i++;
                }
            }
            messages.add(new Message(Method.GET_USERS, Status.OK, users));
        } else {
            messages.add(new Message(Method.GET_USERS, Status.PERM_DEN, null));
        }
        outputHandler.wakeUp();
    }

    public void newAcc(Message message) throws SQLException {
        String login = message.params.get("login");
        String sql = "SELECT * FROM users WHERE login=?";
        PreparedStatement preparedStatement = server.conn.prepareStatement(sql);
        preparedStatement.setString(1, login);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            messages.add(new Message(Method.NEW_ACC, Status.ERROR, null));
        } else {
            sql = "INSERT users(login,password) VALUES (?,?)";
            String password = message.params.get("password");
            preparedStatement = server.conn.prepareStatement(sql);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            messages.add(new Message(Method.NEW_ACC, Status.OK, null));
        }
        outputHandler.wakeUp();
    }

    public void callResponse(Message message) {
        if (login != null) {
            String login = message.params.get("login");
            String ip = message.params.get("ip");
            String port = message.params.get("port");
            ClientHandler clientHandler = server.users.get(login);
            if (clientHandler == null) {
                busy = false;
                messages.add(new Message(Method.CALL, Status.ERROR_USER, null));
                outputHandler.wakeUp();
            } else if (message.status == Status.OK) {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", this.login);
                params.put("port", port);
                params.put("ip", ip);
                //may be sybchronizeed
                //   synchronized (clientHandler.messages) {
                clientHandler.messages.add(new Message(Method.CALL, Status.OK, params));
                clientHandler.outputHandler.wakeUp();
                busy = true;
                // }
            } else if (message.status == Status.REJECTED) {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", this.login);
                params.put("port", port);
                params.put("ip", ip);
                clientHandler.messages.add(new Message(Method.CALL, Status.REJECTED, params));
                clientHandler.outputHandler.wakeUp();
                busy = false;
                clientHandler.busy = false;
            }
        }

    }

    public void endCall(Message message) {
        if (login != null) {
            String login = message.params.get("login");

            ClientHandler clientHandler = server.users.get(login);
            if (clientHandler != null) {
                clientHandler.messages.add(message);
                clientHandler.outputHandler.wakeUp();
                clientHandler.busy = false;
            }
            busy = false;
        }
    }

    private void deleteUser() {
        synchronized (server.users) {
            if (login != null) {
                server.users.remove(login);
                login = null;
            }
        }
    }
}