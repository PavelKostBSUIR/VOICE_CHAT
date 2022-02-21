import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InputThr extends Thread {
    DatagramSocket inputSocket;
    private boolean kill = false;
    private boolean stopped = false;

    private DynThr dynThr;

    InputThr(DatagramSocket inputSocket) {
        this.inputSocket = inputSocket;
    }

    public void kill() {
        kill = true;
        inputSocket.close();
    }

    public void stopDyn() {
        if (dynThr != null) {
            dynThr.kill();
            stopped = true;
            dynThr = null;
        }
    }

    public void startDyn() {
        dynThr = new DynThr();
        dynThr.start();
        stopped = false;
    }

    @Override
    public void run() {
        dynThr = new DynThr();
        dynThr.start();
        while (!kill) {
            byte[] buff = new byte[SoundPacket.dataLen];
            DatagramPacket inputPacket = new DatagramPacket(buff, buff.length);
            try {
                inputSocket.receive(inputPacket);
                SoundPacket packet = new SoundPacket(inputPacket.getData());
                if (!stopped) {
                    dynThr.recievedPackets.add(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dynThr != null) {
            dynThr.kill();
        }
    }
}
