import java.sql.SQLException;

abstract class InputHandlerMethod {
    abstract void handle(Message message) throws SQLException;
}
