import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

public class SoundPacket {
    public static AudioFormat af = new AudioFormat(11025f, 8, 1, true, true); //11.025khz, 8bit, mono, signed, big endian (changes nothing in 8 bit) ~8kb/s
    public static int dataLen = 2000;
    private byte[] data;

    public SoundPacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
