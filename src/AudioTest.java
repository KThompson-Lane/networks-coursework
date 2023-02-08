import java.util.Vector;
import java.util.Iterator;

//Uncomment the following if using JAR package:
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

/**
 * CMPC3M06 Audio Test
 *
 *  This class is designed to test the audio player and recorder.
 * 
 * @author Philip Harding
 */
public class AudioTest {
    public static void main(String args[]) throws Exception {
        //  Audio format information from lab sheet
        /*
            • Sampling rate: 8000Hz (8000 samples per second)
            • Bits per sample: 16 bits (2 bytes)
            • Samples per block: 256 (i.e. 512 bytes)
            • No. channels: 1 (Mono sound)
        */

        //  Vector used to store audio blocks (32ms/512bytes each)
        Vector<byte[]> voiceVector = new Vector<byte[]>();

        //  Initialise AudioPlayer and AudioRecorder objects
        AudioRecorder recorder = new AudioRecorder();
        AudioPlayer player = new AudioPlayer();

        //  Recording time in seconds
        int recordTime = 10;

        //  Capture audio data and add to voiceVector
        System.out.println("Recording Audio...");
        
        for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
            byte[] block = recorder.getBlock();
            voiceVector.add(block);
        }

        //  Close audio input
        recorder.close();

        //  Iterate through voiceVector and play out each audio block
        System.out.println("Playing Audio...");

        Iterator<byte[]> voiceItr = voiceVector.iterator();
        while (voiceItr.hasNext()) {
            player.playBlock(voiceItr.next());
        }

        //Close audio output
        player.close();
    }
}