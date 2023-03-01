package Security;

import java.io.IOException;
import java.nio.ByteBuffer;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;

public class SecurityLayer {
    private final long secretKey;
    private final boolean enableEncryption;
    public SecurityLayer(long secretKey, boolean enableEncryption) {
        this.secretKey = secretKey;
        this.enableEncryption = enableEncryption;
    }

    public byte[] EncryptAndAuth(byte[] dataPacket)
    {
        byte[] securePacket = dataPacket;
        //  Authenticate
            //TODO: Append authentication header
        //  Encrypt
        if(enableEncryption)
            securePacket = SimpleEncryption.EncryptData(dataPacket, secretKey);
        //  Return secure packet
        return securePacket;

    }
    public byte[] DecryptAndAuth(byte[] encryptedPacket)
    {
        byte[] dataPacket = encryptedPacket;
        //  Authenticate
            //TODO: Check authentication header

        //  Decrypt
        if(enableEncryption)
            dataPacket = SimpleEncryption.EncryptData(dataPacket, secretKey);

        //  Return decrypted data packet
        return dataPacket;
    }

    public static void main(String[] args) {
        TestNumber();
        TestAudio();
    }

    public static void TestNumber()
    {
        long key = 832139;
        long input = 123456789;
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
        System.out.println("Decrypted output = " + decryptOutput);
    }

    public static void TestAudio()
    {
        long key = 832139;
        long input = 123456789;

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
            try {
                byte[] block = recorder.getBlock();
                block = SimpleEncryption.EncryptData(block, key);
                if(decrypt)
                    block = SimpleEncryption.DecryptData(block, key);
                player.playBlock(block);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
