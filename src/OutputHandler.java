import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Queue;

public class OutputHandler extends Thread {
    private boolean kill = false;
    private OutputStream out;
    private Queue<Message> messages;
    private ObjectOutputStream objectOutputStream;

    OutputHandler(OutputStream outputStream, Queue<Message> messages) {
        this.out = outputStream;
        this.messages = messages;
        start();
    }

    public void kill() {
        kill = true;
        try {
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void wakeUp() {
        notify();
    }

    @Override
    public synchronized void run() {
        try {
            objectOutputStream = new ObjectOutputStream(this.out);
            while (!kill) {
                synchronized (messages) {

                    if (!messages.isEmpty()) {
                        Message message = messages.remove();
                        objectOutputStream.writeObject(message);
                        objectOutputStream.flush();


                    } else {
                        wait();
                    }
                }
            }
        } catch (
                IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
