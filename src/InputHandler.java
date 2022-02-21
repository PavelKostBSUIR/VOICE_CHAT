import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;

class InputHandler extends Thread {
    private InputStream in;
    private boolean kill = false;
    InputHandlerMethod inputHandlerMethod;
    ObjectInputStream objectInputStream;

    InputHandler(InputStream inputStream, InputHandlerMethod inputHandlerMethod) throws IOException {
        this.in = inputStream;
        this.inputHandlerMethod = inputHandlerMethod;
        start();
    }

    public void kill() {
        this.kill = true;

        try {
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            objectInputStream = new ObjectInputStream(this.in);
            while (!kill) {
                Message message;
                message = (Message) objectInputStream.readObject();
                inputHandlerMethod.handle(message);
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            try {
                inputHandlerMethod.handle(new Message(Method.KILL, Status.USER_DISC, null));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            System.out.println("socket disconnected");
            //e.printStackTrace();
        }
    }
}
