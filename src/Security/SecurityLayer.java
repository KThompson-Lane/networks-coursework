package Security;

import java.nio.ByteBuffer;

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

    public static void main(String[] args) {
        //TEST
        long input = 123456;
        long secretKey = 380031;
        ByteBuffer inputBuff = ByteBuffer.allocate(64);
        ByteBuffer cipherBuff = ByteBuffer.allocate(64);
        ByteBuffer outputBuff = ByteBuffer.allocate(64);

        inputBuff.putLong(input);
        byte[] cipherText = SecurityLayer.EncryptData(inputBuff.array(), secretKey);
        cipherBuff.put(cipherText);
        long cipherInput = cipherBuff.getLong(0);
        byte[] plainText = SecurityLayer.DecryptData(cipherText, secretKey);
        outputBuff.put(plainText);
        long decryptOutput = outputBuff.getLong(0);

        System.out.println("Input = " +input);
        System.out.println("Encrypted input = " + cipherInput);
        System.out.println("Decrypted output = " +decryptOutput);

    }
}
