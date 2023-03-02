package Security;

import java.nio.ByteBuffer;

public class SimpleEncryption {

    private static final int key[] = {
            1, 0, 1, 0, 0, 0, 0, 0, 1, 0
    }; // extra example for checking purpose


    private static final int FKP[] = { 3, 5, 2, 7, 4, 10, 1, 9, 8, 6 };
    private static final int SKP[] = { 6, 3, 7, 4, 8, 5, 10, 9 };

    private static int FirstKey[] = new int[8];
    private static int SecondKey[] = new int[8];

    private static final int[] InitialPermutation = { 2, 6, 3, 1, 4, 8, 5, 7 };
    private static final int[] ExpansionPermutation = { 4, 1, 2, 3, 2, 3, 4, 1 };
    private static final int[] StraightPermutation = { 2, 4, 3, 1 };
    private static final int[] InverseInitialPermutation = { 4, 1, 3, 5, 7, 2, 8, 6 };
    private static final int[][][] SubstitutionTable = {
            {
                { 1, 0, 3, 2 },
                { 3, 2, 1, 0 },
                { 0, 2, 1, 3 },
                { 3, 1, 3, 2 }
            },
            {
                { 0, 1, 2, 3 },
                { 2, 0, 1, 3 },
                { 3, 0, 1, 0 },
                { 2, 1, 0, 3 }
            }
    };

    //  Using FKP and SKP we generate key 1 and key 2 using bit shifts
    public static void key_generation()
    {
        int tempKey[] = new int[10];

        for (int i = 0; i < 10; i++) {
            //  First we permute our 10 bit input key using FKP
            tempKey[i] = key[FKP[i] - 1];
        }

        int left[] = new int[5];
        int right[] = new int[5];

        for (int i = 0; i < 5; i++) {
            left[i] = tempKey[i];
            right[i] = tempKey[i + 5];
        }

        int[] leftPrime = LeftShift(left, 1);
        int[] rightPrime = LeftShift(right, 1);

        for (int i = 0; i < 5; i++) {
            tempKey[i] = leftPrime[i];
            tempKey[i + 5] = rightPrime[i];
        }

        for (int i = 0; i < 8; i++) {
            FirstKey[i] = tempKey[SKP[i] - 1];
        }

        leftPrime = LeftShift(left, 2);
        rightPrime = LeftShift(right, 2);

        for (int i = 0; i < 5; i++) {
            tempKey[i] = leftPrime[i];
            tempKey[i + 5] = rightPrime[i];
        }
        for (int i = 0; i < 8; i++) {
            SecondKey[i] = tempKey[SKP[i] - 1];
        }
    }


    //  Simple left bit shift function taking an input and number of positions to shift
    private static int[] LeftShift(int[] ar, int n)
    {
        while (n > 0) {
            int temp = ar[0];
            for (int i = 0; i < ar.length - 1; i++) {
                ar[i] = ar[i + 1];
            }
            ar[ar.length - 1] = temp;
            n--;
        }
        return ar;
    }

    // decimal to binary string 0-3
    private static String binary_(int val)
    {
        if (val == 0)
            return "00";
        else if (val == 1)
            return "01";
        else if (val == 2)
            return "10";
        else
            return "11";
    }

    //    this function is doing core things like expansion
    //    then xor with desired key then S0 and S1
    //substitution     P4 permutation and again xor     we have used
    //this function 2 times(key-1 and key-2) during
    //encryption and     2 times(key-2 and key-1) during
    //decryption

    private static int[] function_(int[] Input, int[] Key)
    {
        //  Separate our 8 bit input into two 4 bit halves
        int[] left = new int[4];
        int[] right = new int[4];

        for (int i = 0; i < 4; i++) {
            left[i] = Input[i];
            right[i] = Input[i + 4];
        }

        //  Array for storing the result of our expansion permutation
        int[] expansionResult = new int[8];

        for (int i = 0; i < 8; i++) {
            expansionResult[i] = right[ExpansionPermutation[i] - 1];
        }

        //  We XOR our key with our expansion result
        for (int i = 0; i < 8; i++) {
            Input[i] = Key[i] ^ expansionResult[i];
        }


        int[] leftPrime = new int[4];
        int[] rightPrime = new int[4];

        for (int i = 0; i < 4; i++) {
            leftPrime[i] = Input[i];
            rightPrime[i] = Input[i + 4];
        }

        int row, col, val;

        row = Integer.parseInt("" + leftPrime[0] + leftPrime[3], 2);
        col = Integer.parseInt("" + leftPrime[1] + leftPrime[2], 2);
        val = SubstitutionTable[0][row][col];
        String leftString = binary_(val);

        row = Integer.parseInt("" + rightPrime[0] + rightPrime[3], 2);
        col = Integer.parseInt("" + rightPrime[1] + rightPrime[2], 2);
        val = SubstitutionTable[1][row][col];
        String rightString = binary_(val);


        rightPrime = new int[4];
        for (int i = 0; i < 2; i++) {
            char c1 = leftString.charAt(i);
            char c2 = rightString.charAt(i);
            rightPrime[i] = Character.getNumericValue(c1);
            rightPrime[i + 2] = Character.getNumericValue(c2);
        }

        for (int i = 0; i < 4; i++) {
            rightPrime[i] = rightPrime[StraightPermutation[i] - 1];
        }

        for (int i = 0; i < 4; i++) {
            left[i] = left[i] ^ rightPrime[i];
        }

        int[] output = new int[8];
        for (int i = 0; i < 4; i++) {
            output[i] = left[i];
            output[i + 4] = right[i];
        }
        return output;
    }

    //    this function swaps the nibble of size n(4)
    private static int[] swap(int[] array, int n)
    {
        int[] l = new int[n];
        int[] r = new int[n];

        for (int i = 0; i < n; i++) {
            l[i] = array[i];
            r[i] = array[i + n];
        }

        int[] output = new int[2 * n];
        for (int i = 0; i < n; i++) {
            output[i] = r[i];
            output[i + n] = l[i];
        }

        return output;
    }

    //  This function takes an 8 bit segment and encrypts it
    private static int[] Encrypt(int[] segment)
    {
        int[] step1 = new int[8];

        for (int i = 0; i < 8; i++) {
            step1[i] = segment[InitialPermutation[i] - 1];
        }

        int[] step2 = function_(step1, FirstKey);

        int[] step3 = swap(step2, step2.length / 2);

        int[] step4 = function_(step3, SecondKey);

        int[] ciphertext = new int[8];

        for (int i = 0; i < 8; i++) {
            ciphertext[i] = step4[InverseInitialPermutation[i] - 1];
        }

        return ciphertext;
    }

    //  This function takes an 8 bit segment and decrypts it
    private static int[] Decrypt(int[] segment)
    {
        int[] step1 = new int[8];

        for (int i = 0; i < 8; i++) {
            step1[i] = segment[InitialPermutation[i] - 1];
        }

        int[] step2 = function_(step1, SecondKey);

        int[] step3 = swap(step2, step2.length / 2);

        int[] step4 = function_(step3, FirstKey);

        int[] decrypted = new int[8];

        for (int i = 0; i < 8; i++) {
            decrypted[i] = step4[InverseInitialPermutation[i] - 1];
        }
        return decrypted;
    }

    public static byte[] EncryptData(byte[] plaintext)
    {
        ByteBuffer ToEncrypt = ByteBuffer.wrap(plaintext);
        ByteBuffer EncryptedBuff = ByteBuffer.allocate(plaintext.length);
        for (int i = 0; i < plaintext.length; i++) {
            byte inputByte = ToEncrypt.get();
            String bitString = String.format("%8s", Integer.toBinaryString(inputByte & 0xff)).replace(" ", "0");
            int[] bits = new int[8];
            for(int j = 0; j < 8; j++)
            {
                bits[j] = Character.getNumericValue(bitString.charAt(j));
            }
            int[] encryptedBits = Encrypt(bits);
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < 8; j++)
            {
                str.append(encryptedBits[j]);
            }
            byte encryptedByte = (byte) Short.parseShort(str.toString(), 2);
            EncryptedBuff.put(encryptedByte);
        }
        return EncryptedBuff.array();
    }

    public static byte[] DecryptData(byte[] cipherText)
    {
        ByteBuffer ToDecrypt = ByteBuffer.wrap(cipherText);
        ByteBuffer DecryptedBuff = ByteBuffer.allocate(cipherText.length);
        for (int i = 0; i < cipherText.length; i++) {
            byte inputByte = ToDecrypt.get();
            String bitString = String.format("%8s", Integer.toBinaryString(inputByte & 0xff)).replace(" ", "0");
            int[] bits = new int[8];
            for(int j = 0; j < 8; j++)
            {
                bits[j] = Character.getNumericValue(bitString.charAt(j));
            }
            int[] decryptedBits = Decrypt(bits);
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < 8; j++)
            {
                str.append(decryptedBits[j]);
            }
            byte decryptedByte = (byte) Short.parseShort(str.toString(), 2);
            DecryptedBuff.put(decryptedByte);
        }
        return DecryptedBuff.array();
    }

    public static void main(String[] args)
    {

        //  Generate keys
        key_generation();

        byte[] plaintext = "Hello world!".getBytes();

        System.out.println();
        System.out.println("Your plain Text is :");
        for (int i = 0; i < plaintext.length; i++)
            System.out.print(plaintext[i] + " ");

        byte[] ciphertext = EncryptData(plaintext);

        System.out.println();
        System.out.println("Your cipher Text is :");
        for (int i = 0; i < ciphertext.length; i++)
            System.out.print(ciphertext[i] + " ");

        byte[] decrypted = DecryptData(ciphertext);
        System.out.println();
        System.out.println(
                "Your decrypted Text is :"); // printing the
        // decrypted text
        for (int i = 0; i < decrypted.length; i++)
            System.out.print(decrypted[i] + " ");
    }

}
