package net.jacksum.zzadopt.kr.re.nsr.crypto;

import net.jacksum.zzadopt.kr.re.nsr.crypto.mac.HMac;

/**
 * Interface for implementing a MAC
 */
public abstract class Mac {

    /**
     * Initialization
     *
     * @param key the secret key
     */
    public abstract void init(byte[] key);

    /**
     * Resets the object for the MAC computation of a new message
     */
    public abstract void reset();

    /**
     * Adds a message
     *
     * @param msg the message to add
     */
    public abstract void update(byte[] msg);

    /**
     * Computes the MAC of the message that has been added so far
     *
     * @return the MAC value
     */
    public abstract byte[] doFinal();

    /**
     * Computes the MAC including the last message
     *
     * @param msg the last message
     * @return the MAC value
     */
    public byte[] doFinal(byte[] msg) {
        update(msg);
        return doFinal();
    }

    /**
     * Creates an object for the MAC computation
     *
     * @param algorithm the message digest algorithm
     * @return a Mac object
     */
    public static Mac getInstance(Hash.Algorithm algorithm) {
        Hash md = Hash.getInstance(algorithm);
        return new HMac(md);
    }

}
