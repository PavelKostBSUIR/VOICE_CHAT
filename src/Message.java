import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    Method method;
    HashMap<String, String> params;
    Status status;

    Message(Method method, Status status, HashMap<String, String> params) {
        this.status = status;
        this.method = method;
        this.params = params;
    }
}
