package Security;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SimpleEncryption {

    private static final int[] FKP = { 3, 5, 2, 7, 4, 10, 1, 9, 8, 6 };
    private static final int[] SKP = { 6, 3, 7, 4, 8, 5, 10, 9 };
    private static String[] Keys;

    private static String FirstKey;
    private static String SecondKey;

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

    //  Simple left bit shift function taking an input and number of positions to shift
    private static String LeftShift(final String input, final int n)
    {
        final int length = input.length();
        if (length == 0) return "";
        final int shift = (n % length);

        return input.substring(shift, length) + input.substring(0, shift);
    }

    //  Generate keys using permutation tables FKP and SKP and bit-shifts
    public static void GenerateKeys(final long inputKey)
    {
        //  Take our master key and use the first 10 bits to generate our keys
        int hashedKey = Long.hashCode(inputKey);
        String fullKey = ToPaddedBinaryString(hashedKey, 40);
        Keys = new String[4];
        for(int i = 0; i < 4; i++)
        {
            String key = fullKey.substring(i*10, (i+1)*10);
            key = Permute(key, FKP);
            key = LeftShift(key.substring(0,5), i) + LeftShift(key.substring(5), i);
            key = Permute(key, SKP);
            Keys[i] = key;
        }

        /*String input = new StringBuilder(Integer.toBinaryString(hashedKey)).substring(0,10);
        String key;

        //  First we permute our 10 bit input key using FKP
        String masterKey = Permute(input, FKP);

        //  We then begin building our key by left shifting each half by one
        key = LeftShift(masterKey.substring(0,5), 1) + LeftShift(masterKey.substring(5), 1);

        //  Our first key is given by permuting using the SKP
        FirstKey = Permute(key, SKP);

        //  We then left shift our key again by 2
        key = LeftShift(key.substring(0,5), 2) + LeftShift(key.substring(5), 2);

        //  Our second key is again given by permuting using the SKP
        SecondKey = Permute(key, SKP);*/
    }

    //  Helper function that converts an integer to a padded binary string
    private static String ToPaddedBinaryString(final int value, final int padding)
    {
        StringBuilder output = new StringBuilder(Integer.toBinaryString(value));

        //  Prepend zeroes until we reach sufficient padding
        while (output.length() < padding) {
            output.insert(0, "0");
        }
        return output.toString();
    }

    //  Helper function for XOR'ing two binary strings
    private static String XOR(final String fst, final String snd)
    {
        int first = Integer.parseInt(fst,2);
        int second = Integer.parseInt(snd,2);
        return ToPaddedBinaryString(first ^ second, fst.length());
    }

    //  A function which permutes an input using a given permutation table
    private static String Permute(final String input, final int[] table)
    {
        StringBuilder output = new StringBuilder();
        for (int index : table) {
            output.append(input.charAt(index - 1));
        }
        return output.toString();
    }

    //  A function which performs a round on the input data using a given 8-bit key
    private static String Round(final String Input, final String Key)
    {
        //  Separate our 8 bit input string into two 4 bit halves
        String left = Input.substring(0,4);
        String right = Input.substring(4,8);

        //  String for storing the result of our Expansion permutation
        String expansionResult = Permute(right, ExpansionPermutation);

        //  We XOR our key with our expansion result
        String xorResult = XOR(Key, expansionResult);

        //  Splitting our XOR result into left and right halves
        String leftPrime, rightPrime;
        leftPrime = xorResult.substring(0,4);
        rightPrime = xorResult.substring(4,8);

        //  We substitute the left and right halves using our substitution tables to permute rightPrime
        int row, col, Sub1, Sub2;
        row = Integer.parseInt("" + leftPrime.charAt(0) + leftPrime.charAt(3), 2);
        col = Integer.parseInt("" + leftPrime.charAt(1) + leftPrime.charAt(2), 2);
        Sub1 = SubstitutionTable[0][row][col];

        row = Integer.parseInt("" + rightPrime.charAt(0) + rightPrime.charAt(3), 2);
        col = Integer.parseInt("" + rightPrime.charAt(1) + rightPrime.charAt(2), 2);
        Sub2 = SubstitutionTable[1][row][col];

        rightPrime = ToPaddedBinaryString(Sub1, 2) + ToPaddedBinaryString(Sub2, 2);

        String pResult = Permute(rightPrime, StraightPermutation);

        //  The left half is then XOR'ed with the permuted rightPrime
        left = XOR(left, pResult);

        //  We then recombine the halves and return the result
        return left + right;
    }

    //  This function takes an 8 bit segment and encrypts it
    private static String Encrypt(final String segment)
    {
        //  Initial permutation
        String data = Permute(segment, InitialPermutation);

        for (int round = 0; round < 3; round++)
        {
            //  Do N key round
            data = Round(data, Keys[round]);

            //  Swap left and right halves
            data = data.substring(data.length()/2) + data.substring(0, data.length()/2);
        }

        //  Do final round
        data = Round(data, Keys[3]);

        //  Inverse initial permutation
        return Permute(data, InverseInitialPermutation);
    }

    //  This function takes an 8 bit segment and decrypts it
    private static String Decrypt(final String segment)
    {
        //  Initial permutation
        String data = Permute(segment, InitialPermutation);

        for (int round = 3; round > 0; round--)
        {
            //  Do N key round
            data = Round(data, Keys[round]);

            //  Swap left and right halves
            data = data.substring(data.length()/2) + data.substring(0, data.length()/2);
        }

        //  Do final round
        data = Round(data, Keys[0]);

        //  Inverse initial permutation
        return Permute(data, InverseInitialPermutation);
    }

    //  Encrypts the plaintext one byte at a time using our simple block cipher
    public static byte[] EncryptData(byte[] plaintext)
    {
        ByteBuffer ToEncrypt = ByteBuffer.wrap(plaintext);
        ByteBuffer EncryptedBuff = ByteBuffer.allocate(plaintext.length);
        for (int i = 0; i < plaintext.length; i++) {
            byte inputByte = ToEncrypt.get();
            String bitString = String.format("%8s", Integer.toBinaryString(inputByte & 0xff)).replace(" ", "0");
            String encryptedbitString = Encrypt(bitString);
            byte encryptedByte = (byte) Short.parseShort(encryptedbitString, 2);
            EncryptedBuff.put(encryptedByte);
        }
        return EncryptedBuff.array();
    }

    //  Decrypts the ciphertext one byte at a time using our simple block cipher
    public static byte[] DecryptData(byte[] cipherText)
    {
        ByteBuffer ToDecrypt = ByteBuffer.wrap(cipherText);
        ByteBuffer DecryptedBuff = ByteBuffer.allocate(cipherText.length);
        for (int i = 0; i < cipherText.length; i++) {
            byte inputByte = ToDecrypt.get();
            String bitString = String.format("%8s", Integer.toBinaryString(inputByte & 0xff)).replace(" ", "0");
            String decryptedBitString = Decrypt(bitString);
            byte decryptedByte = (byte) Short.parseShort(decryptedBitString, 2);
            DecryptedBuff.put(decryptedByte);
        }
        return DecryptedBuff.array();
    }

    public static void main(String[] args)
    {
        //  Generate keys
        GenerateKeys(832139);

        byte[] plaintext = "Hello World!".getBytes();

        System.out.println("Your plain Text is : \n" + Arrays.toString(plaintext));

        byte[] ciphertext = EncryptData(plaintext);
        System.out.println("\nYour cipher Text is : \n" + Arrays.toString(ciphertext));


        byte[] decrypted = DecryptData(ciphertext);
        System.out.println("\nYour decrypted Text is : \n" + Arrays.toString(decrypted));
    }

}
