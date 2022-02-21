import java.sql.SQLException;

public class ServInpHandlerMethod extends InputHandlerMethod {
    ClientHandler clientHandler;

    ServInpHandlerMethod(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    void handle(Message message) throws SQLException {
        switch (message.method) {

            case AUTHORIZE: {
                clientHandler.authorize(message);
                break;
            }
            case CALL: {
                clientHandler.call(message);
                break;
            }
            case GET_USERS: {
                clientHandler.getUsers(message);
                break;
            }
            case NEW_ACC: {
                clientHandler.newAcc(message);
                break;
            }
            case KILL: {
                clientHandler.kill(message);
                break;
            }
            case LOG_OUT: {
                clientHandler.logOut(message);
                break;
            }
            case INP_CALL_RESP: {
                clientHandler.callResponse(message);
                break;
            }
            case CALL_END: {
                clientHandler.endCall(message);
                break;
            }
        }
    }
}
