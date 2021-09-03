/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 * changes by Johann N. Loefflmann on Dec 6, 2020:
 * - formatted code using IntelliJ's default code formatting standards
 * - added printIntermediateValues() and printOutputCompressionFunction() 
 *   for debugging purposes
 * - added Overwrite annotations
 * - renamed STEP() method to step()
 */
package net.jacksum.zzadopt.de.flexiprovider.core.md;

import org.n16n.sugar.util.ByteSequences;
import net.jacksum.zzadopt.de.flexiprovider.api.MessageDigest;
import net.jacksum.zzadopt.de.flexiprovider.common.util.BigEndianConversions;

/**
 * This is an implementation of the FORK-256 Message digest
 *
 * FORK-256 is a 256-bit hash and is meant to provide 128 bits of security
 * against collision attacks.
 *
 * @author Paul Nguentcheu
 */
public class FORK256 extends MessageDigest {

    /**
     * The algorithm name.
     */
    public static final String ALG_NAME = "FORK256";

    /**
     * Initial hash value H<sup>(0)</sup>. The value were obtained by taking the
     * fractional parts of the square roots of the first eight primes.
     */
    private static final int[] H0 = {0x6a09e667, 0xbb67ae85, 0x3c6ef372,
        0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};

    /**
     * Constant words K<sub>0...63</sub>. These are the first thirty-two bits of
     * the fractional parts of the cube roots of the first sixteen primes.
     */
    private static final int[] K = {0x428a2f98, 0x71374491, 0xb5c0fbcf,
        0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74,
        0x80deb1fe, 0x9bdc06a7, 0xc19bf174,};

    /**
     * Length of the FORK-256 message digest in bytes
     */
    private static final int FORK256_DIGEST_LENGTH = 32;

    /**
     * Input buffer
     */
    private byte[] buffer;

    /**
     * Counter for the number of bytes of the message
     */
    private long count;

    /**
     * Contains the digest value after complete message has been processed
     */
    private int[] H = new int[8];

    /**
     * Used to store intermediate values
     */
    private int a, b, c, d, e, f, g, h;

    /**
     * Default constructor
     */
    public FORK256() {
        buffer = new byte[64];
        reset();
    }

    /**
     * @return the digest length in bytes
     */
    @Override
    public int getDigestLength() {
        return FORK256_DIGEST_LENGTH;
    }

    /**
     * Update the digest using the specified array of bytes, starting at the
     * specified offset.
     *
     * @param input the byte array to use for the update
     * @param offset the offset to start from in the byte array
     * @param len the number of bytes to use
     */
    @Override
    public synchronized void update(byte[] input, int offset, int len) {
        int bufOffset = ((int) count) & 63;
        int copyLen;

        while (len > 0) {
            copyLen = 64 - bufOffset;
            copyLen = (len > copyLen) ? copyLen : len;
            System.arraycopy(input, offset, buffer, bufOffset, copyLen);
            len -= copyLen;
            offset += copyLen;
            count += copyLen;
            bufOffset = (bufOffset + copyLen) & 63;

            if (bufOffset == 0) {
                processBlock();
            }
        }
    }

    /**
     * Update the digest using the specified byte.
     *
     * @param input the byte to use for the update
     */
    @Override
    public synchronized void update(byte input) {
        buffer[(int) count & 63] = input;

        if ((int) (count & 63) == 63) {
            processBlock();
        }
        count++;
    }

    /**
     * Complete the hash computation by performing final operations such as
     * padding.
     *
     * @return the digest value
     */
    @Override
    public synchronized byte[] digest() {
        pad();

        byte[] digestValue = new byte[FORK256_DIGEST_LENGTH];
        for (int i = 0; i < 8; i++) {
            BigEndianConversions.I2OSP(H[i], digestValue, i << 2);
        }

        reset();

        return digestValue;
    }

    /**
     * Reset the digest objects to its initial state.
     */
    @Override
    public void reset() {
        H[0] = H0[0];
        H[1] = H0[1];
        H[2] = H0[2];
        H[3] = H0[3];
        H[4] = H0[4];
        H[5] = H0[5];
        H[6] = H0[6];
        H[7] = H0[7];

        count = 0;
    }

    private int branch = 1;
    private int round = 0;

    private void printIntermediateValues() {

        if (round == 0) {
            printOutputCompressionFunction(String.format("branch %d, round 0", branch));
            round++;
        }
        int[] eightInts = new int[8];
        byte[] mybytes = new byte[32];
        int index = 0;
        eightInts[0] = a;
        eightInts[1] = b;
        eightInts[2] = c;
        eightInts[3] = d;
        eightInts[4] = e;
        eightInts[5] = f;
        eightInts[6] = g;
        eightInts[7] = h;
        for (int x = 0; x < 8; x++) {
            ByteSequences.setIntInByteArray(eightInts[x], mybytes, index);
            index += 4;
        }
        System.out.printf("branch %d, round %d = %s\n", branch, round++, ByteSequences.format(mybytes, false, 4, ' '));
        if (round == 9) {
            branch++;
            round = 0;
        }
        if (branch == 5) {
            branch = 1;
        }

    }

    private void printOutputCompressionFunction(String header) {
        byte[] mybytes = new byte[32];
        int index = 0;
        for (int x = 0; x < 8; x++) {
            ByteSequences.setIntInByteArray(H[x], mybytes, index);
            index += 4;
        }
        System.out.printf("%-17s = %s\n", header, ByteSequences.format(mybytes, false, 4, ' '));
    }

    /**
     * Compute the hash value of the current block and store it in H
     */
    private synchronized void processBlock() {
        int[] W = new int[16];
        int[] tmp1 = new int[8];
        int[] tmp2 = new int[8];
        int[] tmp3 = new int[8];
        int[] tmp4 = new int[8];

        // Calculate expanded message blocks (big endian)
        W[0] = BigEndianConversions.OS2IP(buffer, 0);
        W[1] = BigEndianConversions.OS2IP(buffer, 4);
        W[2] = BigEndianConversions.OS2IP(buffer, 8);
        W[3] = BigEndianConversions.OS2IP(buffer, 12);
        W[4] = BigEndianConversions.OS2IP(buffer, 16);
        W[5] = BigEndianConversions.OS2IP(buffer, 20);
        W[6] = BigEndianConversions.OS2IP(buffer, 24);
        W[7] = BigEndianConversions.OS2IP(buffer, 28);
        W[8] = BigEndianConversions.OS2IP(buffer, 32);
        W[9] = BigEndianConversions.OS2IP(buffer, 36);
        W[10] = BigEndianConversions.OS2IP(buffer, 40);
        W[11] = BigEndianConversions.OS2IP(buffer, 44);
        W[12] = BigEndianConversions.OS2IP(buffer, 48);
        W[13] = BigEndianConversions.OS2IP(buffer, 52);
        W[14] = BigEndianConversions.OS2IP(buffer, 56);
        W[15] = BigEndianConversions.OS2IP(buffer, 60);

        // //// BRANCH_1
        // get initial states;
        a = H[0];
        b = H[1];
        c = H[2];
        d = H[3];
        e = H[4];
        f = H[5];
        g = H[6];
        h = H[7];

        step(W[0], W[1], K[0], K[1]);
        step(W[2], W[3], K[2], K[3]);
        step(W[4], W[5], K[4], K[5]);
        step(W[6], W[7], K[6], K[7]);
        step(W[8], W[9], K[8], K[9]);
        step(W[10], W[11], K[10], K[11]);
        step(W[12], W[13], K[12], K[13]);
        step(W[14], W[15], K[14], K[15]);

        // save values;
        tmp1[0] = a;
        tmp1[1] = b;
        tmp1[2] = c;
        tmp1[3] = d;
        tmp1[4] = e;
        tmp1[5] = f;
        tmp1[6] = g;
        tmp1[7] = h;

        // //// BRANCH_2
        // get initial states;
        a = H[0];
        b = H[1];
        c = H[2];
        d = H[3];
        e = H[4];
        f = H[5];
        g = H[6];
        h = H[7];

        step(W[14], W[15], K[15], K[14]);
        step(W[11], W[9], K[13], K[12]);
        step(W[8], W[10], K[11], K[10]);
        step(W[3], W[4], K[9], K[8]);
        step(W[2], W[13], K[7], K[6]);
        step(W[0], W[5], K[5], K[4]);
        step(W[6], W[7], K[3], K[2]);
        step(W[12], W[1], K[1], K[0]);

        // save values;
        tmp2[0] = a;
        tmp2[1] = b;
        tmp2[2] = c;
        tmp2[3] = d;
        tmp2[4] = e;
        tmp2[5] = f;
        tmp2[6] = g;
        tmp2[7] = h;

        // //// BRANCH_3
        // get initial states;
        a = H[0];
        b = H[1];
        c = H[2];
        d = H[3];
        e = H[4];
        f = H[5];
        g = H[6];
        h = H[7];

        step(W[7], W[6], K[1], K[0]);
        step(W[10], W[14], K[3], K[2]);
        step(W[13], W[2], K[5], K[4]);
        step(W[9], W[12], K[7], K[6]);
        step(W[11], W[4], K[9], K[8]);
        step(W[15], W[8], K[11], K[10]);
        step(W[5], W[0], K[13], K[12]);
        step(W[1], W[3], K[15], K[14]);

        // save values;
        tmp3[0] = a;
        tmp3[1] = b;
        tmp3[2] = c;
        tmp3[3] = d;
        tmp3[4] = e;
        tmp3[5] = f;
        tmp3[6] = g;
        tmp3[7] = h;

        // BRANCH_4
        // get initial states;
        a = H[0];
        b = H[1];
        c = H[2];
        d = H[3];
        e = H[4];
        f = H[5];
        g = H[6];
        h = H[7];

        step(W[5], W[12], K[14], K[15]);
        step(W[1], W[8], K[12], K[13]);
        step(W[15], W[0], K[10], K[11]);
        step(W[13], W[11], K[8], K[9]);
        step(W[3], W[10], K[6], K[7]);
        step(W[9], W[2], K[4], K[5]);
        step(W[7], W[14], K[2], K[3]);
        step(W[4], W[6], K[0], K[1]);

        // save values
        tmp4[0] = a;
        tmp4[1] = b;
        tmp4[2] = c;
        tmp4[3] = d;
        tmp4[4] = e;
        tmp4[5] = f;
        tmp4[6] = g;
        tmp4[7] = h;

        for (int i = 0; i <= 7; i++) {
            H[i] += (tmp1[i] + tmp2[i]) ^ (tmp3[i] + tmp4[i]);
        }

        // printOutputCompressionFunction("output");

    }

    /**
     * Performs the padding and hash the value.
     */
    private void pad() {
        // compute length of message in bits
        long bitLength = count << 3;

        // append single 1-bit trailed by 0-bits to message
        buffer[(int) count & 63] = (byte) 0x80;
        count++;

        if ((int) (count & 63) > 56) {
            for (int i = (int) count & 63; i < 64; i++) {
                buffer[i] = 0;
                count++;
            }
            processBlock();
        } else if ((int) (count & 63) == 0) {
            processBlock();
        }

        for (int i = (int) count & 63; i < 56; i++) {
            buffer[i] = 0;
        }

        // append length of message
        BigEndianConversions.I2OSP(bitLength, buffer, 56);

        // chomp last block
        processBlock();
    }

    /**
     * step function
     */
    private void step(int m1, int m2, int d1, int d2) {
        int tmp, t1, t2, t3, t4;

        t1 = G(e + m2);
        t2 = F(e + m2 + d2);
        t3 = F(a + m1);
        t4 = G(a + m1 + d1);

        tmp = (h + ((t1 << 21) | (t1 >>> 11))) ^ ((t2 << 17) | (t2 >>> 15));
        h = (g + ((t1 << 9) | (t1 >>> 23))) ^ ((t2 << 5) | (t2 >>> 27));
        g = (f + (t1 | (t1 >>> 32))) ^ (t2 | (t2 >>> 32));
        f = e + m2 + d2;
        e = (d + ((t3 << 17) | (t3 >>> 15))) ^ ((t4 << 21) | (t4 >>> 11));
        d = (c + ((t3 << 5) | (t3 >>> 27))) ^ ((t4 << 9) | (t4 >>> 23));
        c = (b + (t3 | (t3 >>> 32))) ^ (t4 | (t4 >>> 32));
        b = a + m1 + d1;
        a = tmp;

        // printIntermediateValues();
    }

    /**
     * F function
     */
    private static int F(int x) {
        return x + (((x << 7) | (x >>> 25)) ^ ((x << 22) | (x >>> 10)));
    }

    /**
     * G function
     */
    private static int G(int x) {
        return x ^ (((x << 13) | (x >>> 19)) + ((x << 27) | (x >>> 5)));
    }

}
