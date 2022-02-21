import javax.sound.sampled.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MicThr extends Thread {
    private boolean kill = false;
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, SoundPacket.af);
    TargetDataLine mic;
    DatagramSocket socket;
    int port;
    InetAddress ip;
    private double amplification = 1;

    public void kill() {
        kill = true;
    }

    MicThr(DatagramSocket socket, int port, InetAddress ip, double amplification) throws LineUnavailableException, UnknownHostException {
        this.socket = socket;
        this.amplification = amplification;
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        this.ip = ip;
        this.port = port;

    }

    @Override
    public void run() {
        try {
            mic.open(SoundPacket.af);
            mic.start();
            while (!kill) {
                byte buff[] = new byte[SoundPacket.dataLen];
                mic.read(buff, 0, buff.length);
                long tot = 0;
                for (int i = 0; i < buff.length; i++) {
                    buff[i] *= amplification;
                    tot += Math.abs(buff[i]);
                }
                tot *= 2.5;
                tot /= buff.length;
                if (tot != 0) {
                    socket.send(new DatagramPacket(buff, buff.length, ip, port));
                }
            }
            mic.close();
        } catch (LineUnavailableException | IOException lineUnavailableException) {

            lineUnavailableException.printStackTrace();
        }
    }
}
