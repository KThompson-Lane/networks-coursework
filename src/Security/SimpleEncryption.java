package Security;

import java.nio.ByteBuffer;

public class SimpleEncryption {

    private static final int[] FKP = { 3, 5, 2, 7, 4, 10, 1, 9, 8, 6 };
    private static final int[] SKP = { 6, 3, 7, 4, 8, 5, 10, 9 };

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

    //  Using FKP and SKP we generate key 1 and key 2 using bit shifts
    public static void GenerateKeys(long inputKey)
    {
        String input = new StringBuilder(Long.toBinaryString(inputKey)).substring(0,10);
        String key;

        //  First we permute our 10 bit input key using FKP
        String masterKey = Permutate(input, FKP);

        //  We then begin building our key by left shifting each half by one
        key = LeftShift(masterKey.substring(0,5), 1) + LeftShift(masterKey.substring(5), 1);

        //  Our first key is given by permuting using the SKP
        FirstKey = Permutate(key, SKP);
        System.out.println("First key is: " + FirstKey);

        //  We then left shift our key again by 2
        key = LeftShift(key.substring(0,5), 2) + LeftShift(key.substring(5), 2);

        //  Our second key is again given by permuting using the SKP
        SecondKey = Permutate(key, SKP);
        System.out.println("Second key is: " + SecondKey);

    }

    // Convert 2 bit number to binary string
    private static String ToBinary(int val)
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

    // Helper function for XOR'ing two binary strings and returning a padded 8 bit binary string
    private static String XOR(String fst, String snd, int padding)
    {
        int first = Integer.parseInt(fst,2);
        int second = Integer.parseInt(snd,2);
        StringBuilder binaryOutput = new StringBuilder(Integer.toBinaryString(first ^ second));
        while (binaryOutput.length() < padding) {
            binaryOutput.insert(0, "0");
        }
        return binaryOutput.toString();
    }

    //  A function which permutes an input using a given permutation table
    private static String Permutate(final String input, final int[] table)
    {
        StringBuilder output = new StringBuilder();
        for (int index : table) {
            output.append(input.charAt(index - 1));
        }
        return output.toString();
    }

    //  A function which performs a round on the input data
    private static String Round(final String Input, final String Key)
    {
        //  Separate our 8 bit input string into two 4 bit halves
        String left = Input.substring(0,4);
        String right = Input.substring(4,8);

        //  String builder for storing the result of our EP
        String expansionResult = Permutate(right, ExpansionPermutation);


        //  We XOR our key with our expansion result
        String xorResult = XOR(Key, expansionResult, 8);

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

        rightPrime = ToBinary(Sub1) + ToBinary(Sub2);

        String pResult = Permutate(rightPrime, StraightPermutation);

        left = XOR(left, pResult, 4);
        return left + right;
    }

    //  This function takes an 8 bit segment and encrypts it
    private static String Encrypt(final String segment)
    {
        //  Initial permutation
        String data = Permutate(segment, InitialPermutation);

        //  First key round
        data = Round(data, FirstKey);

        //  Swapping left and right halves
        data = data.substring(data.length()/2) + data.substring(0, data.length()/2);

        //  Second key round
        data = Round(data, SecondKey);

        //  Inverse initial permutation
        return Permutate(data, InverseInitialPermutation);
    }

    //  This function takes an 8 bit segment and decrypts it
    private static String Decrypt(final String segment)
    {
        //  Initial permutation
        String data = Permutate(segment, InitialPermutation);

        //  Second key round
        data = Round(data, SecondKey);

        //  Swapping left and right halves
        data = data.substring(data.length()/2) + data.substring(0, data.length()/2);

        //  First key round
        data = Round(data, FirstKey);

        //  Inverse initial permutation
        return Permutate(data, InverseInitialPermutation);
    }

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
