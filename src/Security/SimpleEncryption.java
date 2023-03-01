package Security;

import java.nio.ByteBuffer;

public class SimpleEncryption {
    private static int leftRotate(int n, int d) {
        return (n << d) | (n >> (32 - d));
    }
    private static int rightRotate(int n, int d) {
        return (n >> d) | (n << (32 - d));
    }

    public static byte[] EncryptData(byte[] plainText, long keyInput)
    {
        ByteBuffer encryptBuff = ByteBuffer.allocate(plainText.length);
        ByteBuffer plaintext = ByteBuffer.wrap(plainText);
        ByteBuffer keyBuff = ByteBuffer.allocate(8);
        keyBuff.putLong(keyInput);

        int firstKey = keyBuff.getInt(0);
        int secondKey = keyBuff.getInt(4);
        for (int i = 0; i < (plainText.length/4); i++)
        {
            int dataSeg = plaintext.getInt();
            if(i % 2 == 0)
            {
                dataSeg = dataSeg ^ firstKey;
                dataSeg = rightRotate(dataSeg, firstKey % 8);
                dataSeg = dataSeg ^ leftRotate(firstKey, firstKey % 8);
                dataSeg = dataSeg ^ rightRotate(secondKey, secondKey % 8);

            }
            else
            {
                dataSeg = dataSeg ^ secondKey;
                dataSeg = leftRotate(dataSeg, secondKey % 8);
                dataSeg = dataSeg ^ rightRotate(secondKey, secondKey % 8);
                dataSeg = dataSeg ^ leftRotate(firstKey, firstKey % 8);

            }
            encryptBuff.putInt(dataSeg);
        }
        return encryptBuff.array();
    }

    public static byte[] DecryptData(byte[] encryptedData, long keyInput)
    {
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedData);
        ByteBuffer decryptBuff = ByteBuffer.allocate(encryptedData.length);
        ByteBuffer keyBuff = ByteBuffer.allocate(8);
        keyBuff.putLong(keyInput);

        int firstKey = keyBuff.getInt(0);
        int secondKey = keyBuff.getInt(4);
        for (int i = 0; i < (encryptedData.length/4); i++)
        {
            int dataSeg = cipherText.getInt();
            if(i % 2 == 0)
            {
                dataSeg = dataSeg ^ leftRotate(secondKey, secondKey % 8);
                dataSeg = dataSeg ^ rightRotate(firstKey, firstKey % 8);
                dataSeg = leftRotate(dataSeg, firstKey % 8);
                dataSeg = dataSeg ^ firstKey;
            }
            else
            {
                dataSeg = dataSeg ^ rightRotate(firstKey, firstKey % 8);
                dataSeg = dataSeg ^ leftRotate(secondKey, secondKey % 8);
                dataSeg = rightRotate(dataSeg, secondKey % 8);
                dataSeg = dataSeg ^ secondKey;
            }
            decryptBuff.putInt(dataSeg);
        }
        return decryptBuff.array();
    }

}
