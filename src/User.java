import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    String login;
    String statement;

    User(String login, String statement) {
        this.login = login;
        this.statement = statement;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getLogin() {
        return login;
    }

    public String getStatement() {
        return statement;
    }
}