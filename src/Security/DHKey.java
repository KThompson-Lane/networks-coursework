package Security;

import java.math.BigInteger;
import java.security.SecureRandom;
public class DHKey {
    //  Integer values for the Prime number P, generator value G, and a random private value seed
    //TODO: Re-implement with BigIntegers for better security
    private final long P, G, seed;
    public DHKey() {
        //TODO: Replace with a safe prime
        this.P = 1015559;
        this.G = 2;
        //TODO: Replace with a better random generator
        SecureRandom random = new SecureRandom();
        this.seed = random.nextLong(2l, P-1);
    }

    private static long modPow(long g, long seed, long p)
    {
        //  We convert these to BigIntegers to prevent precision errors
        BigInteger G = BigInteger.valueOf(g);
        BigInteger Seed = BigInteger.valueOf(seed);
        BigInteger P = BigInteger.valueOf(p);
        BigInteger test = G.modPow(Seed, P);
        return test.longValue();
    }

    public long GetPublicKey()
    {
        //  Using the private seed and the public shared values P,G
        //  We generate a public key G^seed mod P
        return modPow(G, seed, P);
    }
    public long GetSecretKey(long otherKey)
    {
        //  Using the other parties public key we generate a shared private key
        //  otherKey^seed mod P
        return modPow(otherKey, seed, P);
    }

    //Methods for testing the DHKey generator functionality
    public static void main(String[] args)
    {
        DHKey hostKey = new DHKey();
        DHKey clientKey = new DHKey();
        long hostSecret = hostKey.GetSecretKey(clientKey.GetPublicKey());
        long clientSecret = clientKey.GetSecretKey(hostKey.GetPublicKey());
        System.out.println("Secret key for the Host is:" + hostSecret);
        System.out.println("Secret key for the Client is:" + clientSecret);
    }
    public static void test() {
        int tests = 100;
        for (int i = 0; i < tests; i++) {
            DHKey hostKey = new DHKey();
            DHKey clientKey = new DHKey();
            long hostSecret = hostKey.GetSecretKey(clientKey.GetPublicKey());
            long clientSecret = clientKey.GetSecretKey(hostKey.GetPublicKey());
            if(hostSecret != clientSecret)
            {
                System.out.println("shared key mismatch!!");
            }
        }
    }
}
