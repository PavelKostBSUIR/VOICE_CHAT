

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Queue;

public class DynThr extends Thread {
    private boolean kill = false;
    Queue<SoundPacket> recievedPackets = new ArrayDeque<>();

    DynThr() {
    }

    public void kill() {
        kill = true;
    }

    @Override
    public void run() {
        SourceDataLine speaker;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, SoundPacket.af);
        try {
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(SoundPacket.af);
            speaker.start();
            while (!kill) {
                if (!recievedPackets.isEmpty()) {
                    SoundPacket packet = recievedPackets.remove();
                    speaker.write(packet.getData(), 0, packet.getData().length);
                } else {
                    byte[] noise = new byte[SoundPacket.dataLen];
                    for (int i = 0; i < noise.length; i++) {
                        noise[i] = (byte) ((Math.random() * 3) - 1);
                    }
                    speaker.write(noise, 0, noise.length);
                }
            }
            speaker.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
