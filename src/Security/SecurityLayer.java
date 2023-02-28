package Security;

import java.io.IOException;
import java.nio.ByteBuffer;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;

public class SecurityLayer {

    public static void main(String[] args)
    {
        try {
            TestAudio(832139);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void TestNumber(int input, int key)
    {
        ByteBuffer inputBuff = ByteBuffer.allocate(64);
        ByteBuffer cipherBuff = ByteBuffer.allocate(64);
        ByteBuffer outputBuff = ByteBuffer.allocate(64);
        
        inputBuff.putLong(input);
        byte[] cipherText = SimpleEncryption.EncryptData(inputBuff.array(), key);
        cipherBuff.put(cipherText);
        long cipherInput = cipherBuff.getLong(0);
        byte[] plainText = SimpleEncryption.DecryptData(cipherText, key);
        outputBuff.put(plainText);
        long decryptOutput = outputBuff.getLong(0);

        System.out.println("Input = " +input);
        System.out.println("Encrypted input = " + cipherInput);
        System.out.println("Decrypted output = " +decryptOutput);
    }
    public static void TestAudio(int key) throws IOException
    {
        boolean decrypt = false;
        AudioRecorder recorder = null;
        AudioPlayer player = null;
        try{
            recorder = new AudioRecorder();
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: AudioSender: Could not start audio recorder.");
            e.printStackTrace();
            System.exit(0);
        }
        while(true)
        {
            byte[] block = recorder.getBlock();
            byte[] encryptedAudio = SimpleEncryption.EncryptData(block, key);
            byte[] decryptedAudio = SimpleEncryption.DecryptData(encryptedAudio, key);
            if(decrypt)
            {
                player.playBlock(decryptedAudio);
            }
            else {
                player.playBlock(encryptedAudio);
            }
        }
    }
}
