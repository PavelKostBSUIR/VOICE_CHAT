import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class VoiceSession {
    private DatagramSocket outputSocket;
    private DatagramSocket inputSocket;
    private double amplification = 1;
    private boolean kill = false;
    private boolean inputStarted = false;
    private boolean outputStarted = false;
    private InetAddress ipOut;
    private int outputPort;
    private MicThr micThr;
    private InputThr inputThr;

    VoiceSession(int outputPort, InetAddress ipOut, DatagramSocket inputSocket) throws IOException {
        outputSocket = new DatagramSocket();
        this.inputSocket = inputSocket;
        this.ipOut = ipOut;
        this.outputPort = outputPort;

    }

    public void start() throws LineUnavailableException, UnknownHostException {
        startMic();
        startInp();
    }

    public int getOutputSocketPort() {
        return outputSocket.getPort();
    }

    public int getInputSocketPort() {
        return inputSocket.getPort();
    }

    public InetAddress getInetAdress() {
        return inputSocket.getInetAddress();
    }

    public void setAmplification(double amplification) {
        this.amplification = amplification;
    }

    public void kill() {
        if (outputStarted) {
            if (micThr != null) {
                micThr.kill();
            }
        }
        if (inputStarted) {
            inputThr.kill();
        }
        outputSocket.close();
        inputSocket.close();
    }

    public boolean offMic() {
        if (outputStarted) {
            outputStarted = false;
            micThr.kill();
            return true;
        } else {
            return false;
        }
    }

    public boolean offInp() {
        if (inputStarted) {
            inputStarted = false;
            inputThr.kill();
            return true;
        } else {
            return false;
        }
    }

    public boolean startMic() throws LineUnavailableException, UnknownHostException {
        if (outputStarted) {
            return false;
        } else {
            micThr = new MicThr(outputSocket, outputPort, ipOut, amplification);
            micThr.start();
            outputStarted = true;
            return true;
        }
    }

    public boolean stopDyn() {
        inputThr.stopDyn();
        return true;
    }

    public boolean startDyn() {
        inputThr.startDyn();
        return true;
    }

    public boolean startInp() {
        if (inputStarted) {
            return false;
        } else {
            inputThr = new InputThr(inputSocket);
            inputThr.start();
            inputStarted = true;
            return true;
        }
    }
}
