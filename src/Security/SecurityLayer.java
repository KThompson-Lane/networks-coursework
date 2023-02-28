package Security;

import java.io.IOException;
import java.nio.ByteBuffer;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;

public class SecurityLayer {
    public static byte[] EncryptData(byte[] plainText, long keyInput)
    {

        ByteBuffer encryptBuff = ByteBuffer.allocate(plainText.length);
        ByteBuffer plaintext = ByteBuffer.wrap(plainText);

        for (int i = 0; i < (plainText.length/8); i++)
        {
            long dataSeg = plaintext.getLong();
            dataSeg = dataSeg ^ keyInput;
            encryptBuff.putLong(dataSeg);
        }

        return encryptBuff.array();
    }

    public static byte[] DecryptData(byte[] encryptedData, long keyInput)
    {
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedData);
        ByteBuffer decryptBuff = ByteBuffer.allocate(encryptedData.length);
        for (int i = 0; i < (encryptedData.length/8); i++)
        {
            long encryptedSegment = cipherText.getLong();
            encryptedSegment = encryptedSegment ^ keyInput;
            decryptBuff.putLong(encryptedSegment);
        }
        return decryptBuff.array();
    }

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
        byte[] cipherText = SecurityLayer.EncryptData(inputBuff.array(), key);
        cipherBuff.put(cipherText);
        long cipherInput = cipherBuff.getLong(0);
        byte[] plainText = SecurityLayer.DecryptData(cipherText, key);
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
            byte[] encryptedAudio = SecurityLayer.EncryptData(block, key);
            byte[] decryptedAudio = SecurityLayer.DecryptData(encryptedAudio, key);
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
