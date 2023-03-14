package Security;

import java.io.IOException;
import java.nio.ByteBuffer;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;

public class SecurityLayer {
    private final Authenticator authenticator;
    private final boolean enableEncryption;
    public SecurityLayer(long secretKey, boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
        authenticator = new Authenticator(Long.hashCode(secretKey));
        SimpleEncryption.GenerateKeys(secretKey);
    }

    public byte[] EncryptAndSign(byte[] dataPacket)
    {
        byte[] securePacket = dataPacket;
        //  Encrypt
        if(enableEncryption)
            securePacket = SimpleEncryption.EncryptData(dataPacket);
        //  Authenticate
        securePacket = authenticator.SignPacket(securePacket);
        //  Return secure packet
        return securePacket;
    }

    public byte[] AuthAndDecrypt(byte[] encryptedPacket) throws UnableToAuthenticateException {

        ByteBuffer EncryptedPacketBuff = ByteBuffer.wrap(encryptedPacket);

        //  Try and authenticate, but do not catch an unable to authenticate exception
        byte[] authedPacket = authenticator.Authenticate(EncryptedPacketBuff.array());

        //  After authenticating decrypt packet
        if(enableEncryption)
            authedPacket = SimpleEncryption.DecryptData(authedPacket);

        //  Return decrypted data packet
        return authedPacket;
    }

    //Methods for testing the security layer functionality
    public static void main(String[] args) {
        SimpleEncryption.GenerateKeys(832139);
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
        byte[] cipherText = SimpleEncryption.EncryptData(inputBuff.array());
        cipherBuff.put(cipherText);
        long cipherInput = cipherBuff.getLong(0);
        byte[] plainText = SimpleEncryption.DecryptData(cipherText);
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
                block = SimpleEncryption.EncryptData(block);
                if(decrypt)
                    block = SimpleEncryption.DecryptData(block);
                player.playBlock(block);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
