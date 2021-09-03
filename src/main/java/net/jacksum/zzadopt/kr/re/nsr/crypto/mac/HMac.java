package net.jacksum.zzadopt.kr.re.nsr.crypto.mac;

import java.util.Arrays;

import net.jacksum.zzadopt.kr.re.nsr.crypto.Hash;
import net.jacksum.zzadopt.kr.re.nsr.crypto.Mac;

/**
 * HMAC implementation
 */
public class HMac extends Mac {

    private static final byte IPAD = 0x36;
    private static final byte OPAD = 0x5c;

    private int blocksize;
    private Hash digest;

    private byte[] i_key_pad;
    private byte[] o_key_pad;

    /**
     * Constructor
     *
     * @param md MessageDigest object
     */
    public HMac(Hash md) {
        if (md == null) {
            throw new IllegalArgumentException("md should not be null");
        }

        digest = md.newInstance();
        blocksize = digest.getBlockSize();

        i_key_pad = new byte[blocksize];
        o_key_pad = new byte[blocksize];
    }

    /**
     * Initialize internal state
     *
     * @param key Secret key
     */
    @Override
    public void init(byte[] key) {

        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }

        if (key.length > blocksize) {
            digest.reset();
            key = digest.doFinal(key);
        }

        Arrays.fill(i_key_pad, IPAD);
        Arrays.fill(o_key_pad, OPAD);
        for (int i = 0; i < key.length; ++i) {
            i_key_pad[i] ^= (byte) (key[i]);
            o_key_pad[i] ^= (byte) (key[i]);
        }

        reset();
    }

    /**
     * Initialize the hash function and put i_key_pad in the hash function
     */
    @Override
    public void reset() {
        digest.reset();
        digest.update(i_key_pad);
    }

    /**
     * Put the message into hash function to calculate MAC
     */
    @Override
    public void update(byte[] msg) {
        if (msg == null) {
            return;
        }
        digest.update(msg);
    }

    /**
     * Calculate H(i_key_pad || msg), and H(o_key_pad || H(i_key_pad || msg)).
     */
    @Override
    public byte[] doFinal() {
        byte[] result = digest.doFinal();
        digest.reset();
        digest.update(o_key_pad);
        result = digest.doFinal(result);

        reset();
        return result;
    }

    public static byte[] digest(Hash.Algorithm algorithm, byte[] key, byte[] msg) {
        Hash hash = Hash.getInstance(algorithm);
        HMac hmac = new HMac(hash);
        hmac.init(key);
        return hmac.doFinal(msg);
    }
}
