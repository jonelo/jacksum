package net.jacksum.zzadopt.kr.re.nsr.crypto;

import net.jacksum.zzadopt.kr.re.nsr.crypto.hash.LSH256;
import net.jacksum.zzadopt.kr.re.nsr.crypto.hash.LSH512;

/**
 * Derived from https://seed.kisa.or.kr/kisa/Board/22/detailView.do
 * SHA3-256(Hash function LSH_source code.zip (v1.0.1))=
 * 9bd824fa734635ecc9dbbfde737a7ee2c8f74010c96cda482180e780b9b54797 All Korean
 * has been translated to English by johann.loefflmann.net
 */
/**
 * LSH256, LSH512 This class contains a common interface for implementing hash
 * functions.
 */
public abstract class Hash {

    /**
     * LSH Algorithm specification
     */
    public enum Algorithm {
        /**
         * LSH-256-224 Algorithm specification
         */
        LSH256_224,
        /**
         * LSH-256-256 Algorithm specification
         */
        LSH256_256,
        /**
         * LSH-512-224 Algorithm specification
         */
        LSH512_224,
        /**
         * LSH-512-256 Algorithm specification
         */
        LSH512_256,
        /**
         * LSH-512-384 Algorithm specification
         */
        LSH512_384,
        /**
         * LSH-512-512 Algorithm specification
         */
        LSH512_512
    };

    /**
     * Creates and returns an object with the same output length.
     *
     * @return an LSHDigest object
     */
    public abstract Hash newInstance();

    /**
     * Returns the message block bit length used for internal calculations.
     *
     * @return the length of the message block in bits
     */
    public abstract int getBlockSize();

    /**
     * Returns the output length of the hash.
     *
     * @return the output length of the hash (in bits)
     */
    public abstract int getOutlenbits();

    /**
     * Resets the internal state in order to prepare the computation of a new message digest.
     */
    public abstract void reset();

    /**
     * Processes data for the computation of the message digest.
     *
     * @param data the data for the message digest
     * @param offset starting offset of the data array
     * @param lenbits length of the data (in bits)
     */
    public abstract void update(byte[] data, int offset, int lenbits);

    /**
     * Computes the message digest
     *
     * @return the computed message digest value
     */
    public abstract byte[] doFinal();

    /**
     * Processes data for the computation of the message digest.
     *
     * @param data the data for the message digest
     */
    public void update(byte[] data) {
        if (data != null) {
            update(data, 0, data.length * 8);
        }
    }

    /**
     * Adds the data and computes the final message digest.
     *
     * @param data the data for the message digest
     * @param offset starting offset of the data array
     * @param lenbits length of the data (in bits)
     * @return returns the digest
     */
    public byte[] doFinal(byte[] data, int offset, int lenbits) {
        if (data != null && lenbits > 0) {
            update(data, offset, lenbits);
        }

        return doFinal();
    }

    /**
     * Adds the data and computes the final message digest.
     *
     * @param data the final data
     * @return the computed message digest
     */
    public byte[] doFinal(byte[] data) {
        if (data != null) {
            update(data);
        }

        return doFinal();
    }

    /**
     * Returns the hash function object for the algorithm
     *
     * @param algorithm the algorithm
     * @return the hash function object
     */
    public static Hash getInstance(Algorithm algorithm) {

        Hash lsh = null;

        switch (algorithm) {

            case LSH256_224:
                lsh = new LSH256(224);
                break;

            case LSH256_256:
                lsh = new LSH256(256);
                break;

            case LSH512_224:
                lsh = new LSH512(224);
                break;

            case LSH512_256:
                lsh = new LSH512(256);
                break;

            case LSH512_384:
                lsh = new LSH512(384);
                break;

            case LSH512_512:
                lsh = new LSH512(512);
                break;

            default:
                throw new RuntimeException("Unsupported algorithm");
        }

        return lsh;
    }

    /**
     * Computes the hash with the algorithm
     *
     * @param algorithm the algorithm
     * @param data the data to hash
     * @return a byte array.
     */
    public static byte[] digest(Algorithm algorithm, byte[] data) {
        return digest(algorithm, data, 0, data == null ? 0 : data.length << 3);
    }

    /**
     * Computes the hash with the algorithm
     *
     * @param algorithm the algorithm
     * @param data the data to hash
     * @param offset starting offset of the data
     * @param lenbits length of the data, in bits
     * @return a byte array.
     */
    public static byte[] digest(Algorithm algorithm, byte[] data, int offset,
            int lenbits) {
        Hash lsh = Hash.getInstance(algorithm);
        lsh.update(data, offset, lenbits);
        return lsh.doFinal();
    }

    /**
     * Computes the hash with the algorithm LSH-wordlenbits-hashlenbits
     *
     * @param wordlenbits word length, in bits, 256 or 512
     * @param hashlenbits output length, in bits, 1 to wordlenbits
     * @param data the data to hash
     * @return the hash value
     */
    public static byte[] digest(int wordlenbits, int hashlenbits, byte[] data) {
        return digest(wordlenbits, hashlenbits, data, 0, data == null ? 0
                : data.length * 8);
    }

    /**
     *
     * @param wordlenbits Word length, in bits, 256 or 512
     * @param hashlenbits output length, in bits, 1 to wordlenbits
     * @param data the data to hash
     * @param offset starting offset of the data
     * @param lenbits length of the data, in bits
     * @return the hash value
     */
    public static byte[] digest(int wordlenbits, int hashlenbits, byte[] data,
            int offset, int lenbits) {
        Hash lsh = null;
        switch (wordlenbits) {
            case 256:
                lsh = new LSH256(hashlenbits);
                break;
            case 512:
                lsh = new LSH512(hashlenbits);
                break;
            default:
                throw new RuntimeException("Unsupported wordlenbits");
        }

        lsh.update(data, offset, lenbits);
        return lsh.doFinal();
    }

}
