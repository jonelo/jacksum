/*
 * Copyright (c) 1998-2007 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 * Changes by Johann N. Loefflmann in Dec 2020:
 * - added Overwrite annotations
 * - added extendByteArray in order to guarantee digest length to be VSH_DIGEST_LENGTH
 * - coded formatted using IntelliJ standard code formatting guidelines
 */
package net.jacksum.zzadopt.de.flexiprovider.core.md;

import java.util.Vector;

import net.jacksum.zzadopt.de.flexiprovider.api.MessageDigest;
import net.jacksum.zzadopt.de.flexiprovider.common.math.FlexiBigInt;
import net.jacksum.zzadopt.de.flexiprovider.common.math.IntegerFunctions;
import net.jacksum.zzadopt.de.flexiprovider.common.util.FlexiBigIntUtils;

/**
 * This is an implementation of the Basic VSH Message digest
 *
 * @author Paul Nguentcheu
 */
public class VSH extends MessageDigest {

    /**
     * The algorithm name.
     */
    public static final String ALG_NAME = "VSH";

    /**
     * Inner class for representing bit strings.
     */
    protected static class Bitstring {

        private int len; // number of bits stored in this Bitstring
        private int blocks; // number of int used in value
        private int[] value; // storage

        private static final int[] reverseRightMask = { // pre-computed Bitmask
            // for fast masking,
            // rightMask[a]=0xffffffff >>> (32-a)
            0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000f,
            0x0000001f, 0x0000003f, 0x0000007f, 0x000000ff, 0x000001ff,
            0x000003ff, 0x000007ff, 0x00000fff, 0x00001fff, 0x00003fff,
            0x00007fff, 0x0000ffff, 0x0001ffff, 0x0003ffff, 0x0007ffff,
            0x000fffff, 0x001fffff, 0x003fffff, 0x007fffff, 0x00ffffff,
            0x01ffffff, 0x03ffffff, 0x07ffffff, 0x0fffffff, 0x1fffffff,
            0x3fffffff, 0x7fffffff, 0xffffffff};

        private static final int[] bitMask = { // pre-computed Bitmask for fast
            // masking, bitMask[a]=0x1 << a
            0x00000001, 0x00000002, 0x00000004, 0x00000008, 0x00000010,
            0x00000020, 0x00000040, 0x00000080, 0x00000100, 0x00000200,
            0x00000400, 0x00000800, 0x00001000, 0x00002000, 0x00004000,
            0x00008000, 0x00010000, 0x00020000, 0x00040000, 0x00080000,
            0x00100000, 0x00200000, 0x00400000, 0x00800000, 0x01000000,
            0x02000000, 0x04000000, 0x08000000, 0x10000000, 0x20000000,
            0x40000000, 0x80000000, 0x00000000};

        /**
         * Creates a new Bitstring by converting the given byte[] <i>os</i>
         * according to 1363 and using the given <i>length</i>.
         *
         * @param length the length
         * @param os the octet string to assign to this bitstring
         * @see "P1363 5.5.2 p22f, OS2BSP"
         */
        public Bitstring(int length, byte[] os) {
            int l = length;
            if (l < 1) {
                l = 1;
            }
            this.blocks = ((l - 1) >>> 5) + 1;
            this.value = new int[this.blocks];
            this.len = l;
            int i, m;
            int k = Math.min(((os.length - 1) >>> 2) + 1, this.blocks);
            for (i = 0; i < k - 1; i++) {
                m = os.length - (i << 2) - 1;
                this.value[i] = ((os[m])) & 0x000000ff;
                this.value[i] |= ((os[m - 1] << 8)) & 0x0000ff00;
                this.value[i] |= ((os[m - 2] << 16)) & 0x00ff0000;
                this.value[i] |= ((os[m - 3] << 24)) & 0xff000000;
            }
            i = k - 1;
            m = os.length - (i << 2) - 1;
            this.value[i] = os[m] & 0x000000ff;
            if (m > 0) {
                this.value[i] |= ((os[m - 1] << 8)) & 0x0000ff00;
            }
            if (m > 1) {
                this.value[i] |= ((os[m - 2] << 16)) & 0x00ff0000;
            }
            if (m > 2) {
                this.value[i] |= ((os[m - 3] << 24)) & 0xff000000;
            }
            if ((this.len & 0x1f) != 0) {
                this.value[this.blocks - 1] &= reverseRightMask[this.len & 0x1f];
            }
            reduceN();
        }

        /**
         * Construct a new bit string by converting the given FlexiBigInt
         * <i>bi</i> according to 1363 and using the given <i>length</i>.
         *
         * @param length the length of this bit string
         * @param bi the FlexiBigInt to assign to this bit string
         * @see "P1363 5.5.1 p22, I2BSP"
         *
         */
        public Bitstring(int length, FlexiBigInt bi) {
            int l = length;
            if (l < 1) {
                l = 1;
            }
            this.blocks = ((l - 1) >>> 5) + 1;
            this.value = new int[this.blocks];
            this.len = l;
            int i;
            byte[] val = bi.toByteArray();
            if (val[0] == 0) {
                byte[] dummy = new byte[val.length - 1];
                System.arraycopy(val, 1, dummy, 0, dummy.length);
                val = dummy;
            }
            int ov = val.length & 0x03;
            int k = ((val.length - 1) >>> 2) + 1;
            for (i = 0; i < ov; i++) {
                this.value[k - 1] |= ((val[i]) & 0x000000ff) << ((ov - 1 - i) << 3);
            }
            int m = 0;
            for (i = 0; i <= (val.length - 4) >> 2; i++) {
                m = val.length - 1 - (i << 2);
                this.value[i] = (val[m]) & 0x000000ff;
                this.value[i] |= ((val[m - 1]) << 8) & 0x0000ff00;
                this.value[i] |= ((val[m - 2]) << 16) & 0x00ff0000;
                this.value[i] |= ((val[m - 3]) << 24) & 0xff000000;
            }
            if ((len & 0x1f) != 0) {
                this.value[this.blocks - 1] &= reverseRightMask[this.len & 0x1f];
            }
            reduceN();
        }

        /**
         * Reduces this.len by finding the most significant bit set to one and
         * reducing len and blocks.
         */
        public void reduceN() {
            int i, j, h;
            i = this.blocks - 1;
            while ((this.value[i] == 0) && (i > 0)) {
                i--;
            }
            h = this.value[i];
            j = 0;
            while (h != 0) {
                h >>>= 1;
                j++;
            }
            this.len = (i << 5) + j;
            this.blocks = i + 1;
        }

        /**
         * Get the bit at the specified position.
         *
         * @param i the bit position
         * @return the bit at the specified position
         */
        public int getBit(int i) {
            if (i < 0 || i > (this.len - 1)) {
                return 0;
            }
            return ((this.value[i >>> 5] & bitMask[i & 0x1f]) != 0) ? 1 : 0;
        }
    }

    // buffer used to store bytes for processing
    private byte[] buffer;

    // counts the number of bytes of the message
    private long count;

    // the modulus used to compute the message digest
    private static final FlexiBigInt n = new FlexiBigInt(
            "1350664108659952233496032162788059699388814756056670275244851438515"
            + "265106048595338339402871505719094417982072821644715513736804197039641917430464965"
            + "892742562393410208643832021103729587257623585096431105640735015081875106765946292"
            + "05563685529475213500852879416377328533906109750544334999811150056977236890927563");

    // the maximal number of primes used to compute the message digest
    private int k;

    // the digest length in bytes
    private static final int VSH_DIGEST_LENGTH = (n.bitLength() + 7) >>> 3;

    // the internal state
    private FlexiBigInt state;

    // contains the prime numbers
    private Vector v;

    // k modulo 8
    private int r;

    // Contains the number of k blocks we need to obtain a multiple of 8 and
    // depends of the rest of k modulo 8 e.g if k % 8 == 2 then we need four
    // blocks a[2] = 4.
    private byte[] arr = {1, 8, 4, 8, 2, 8, 4, 8};

    // Block length used in the program to compute the message. It can be
    // different of k if k % 8 !=0 b = arr[r] * k / 8 with r = k % 8
    private int b;

    /**
     * Constructor.
     */
    public VSH() {
        // block length in bits
        k = computePrimes(n);
        r = k & 7;
        // new block length in bytes
        b = (arr[r] * k) >> 3;
        buffer = new byte[b];
        reset();
    }

    /**
     * @return the digest length in bytes
     */
    @Override
    public int getDigestLength() {
        return VSH_DIGEST_LENGTH;
    }

    /**
     * Updates the digest using the specified array of bytes, starting at the
     * specified offset.
     *
     * @param input the byte[] to use for the update.
     * @param offset the offset to start from in the array of bytes.
     * @param len the number of bytes to use, starting at offset.
     */
    @Override
    public synchronized void update(byte[] input, int offset, int len) {
        int bufOffset = ((int) count) % b;
        int copyLen;

        while (len > 0) {
            copyLen = b - bufOffset;
            copyLen = (len > copyLen) ? copyLen : len;
            System.arraycopy(input, offset, buffer, bufOffset, copyLen);
            len -= copyLen;
            offset += copyLen;
            count += copyLen;
            bufOffset = (bufOffset + copyLen) % b;

            if (bufOffset == 0) {
                processBlock(arr[r]);
            }
        }
    }

    /**
     * Updates the digest using the specified byte.
     *
     * @param input - the byte to use for the update.
     */
    @Override
    public synchronized void update(byte input) {
        buffer[(int) count % b] = input;

        if ((int) (count % b) == (b - 1)) {
            processBlock(arr[r]);
        }
        count++;
    }

    /**
     * Resets the digest for further use.
     */
    @Override
    public synchronized void reset() {
        count = 0;
        state = FlexiBigInt.ONE;
    }

    /**
     * Completes the hash computation by performing final operations such as
     * padding.
     *
     * @return the digest value
     */
    @Override
    public synchronized byte[] digest() {
        pad();

        // append length of message in bits at end of message in a new block
        long bitLength = count << 3;
        Bitstring arrBitLength = new Bitstring(k, FlexiBigInt
                .valueOf(bitLength));

        FlexiBigInt p = FlexiBigInt.ONE;
        for (int i = 0; i < k; i++) {
            p = p.multiply(
                    ((FlexiBigInt) v.elementAt(i + 1)).pow(arrBitLength
                            .getBit(i))).mod(n);
        }

        FlexiBigInt result = state.multiply(state).mod(n).multiply(p).mod(n);
        //byte[] digestValue = FlexiBigIntUtils.toMinimalByteArray(result);
        byte[] digestValue = extendByteArray(FlexiBigIntUtils.toMinimalByteArray(result), VSH_DIGEST_LENGTH);

        reset();
        return digestValue;
    }

    // added by Johann N. Loefflmann
    public static byte[] extendByteArray(byte[] inputArray, int length) {
        if (inputArray.length >= length) {
            return inputArray;
        }
        byte[] result = new byte[length];
        System.arraycopy(inputArray, 0, result, length - inputArray.length, inputArray.length);
        return result;
    }

    /**
     * Compute primes such that p1*p2*p3*...*pk < n with p1=2,p2=3,p3=5,... and
     * k is maximal.
     *
     * @param n the modulus
     * @return the number of primes
     */
    private int computePrimes(FlexiBigInt n) {
        FlexiBigInt prime = FlexiBigInt.ONE;
        FlexiBigInt primeProduct = FlexiBigInt.ONE;
        v = new Vector();

        v.addElement(FlexiBigInt.valueOf(-1));

        while (n.max(primeProduct).equals(n)) {
            prime = IntegerFunctions.nextPrime(prime.longValue());
            primeProduct = primeProduct.multiply(prime);
            v.addElement(prime);
        }
        v.removeElementAt(v.size() - 1);

        return v.size() - 1; // v.size()-1 because the first element is -1
    }

    /**
     * Perform the padding.
     */
    private void pad() {
        // compute length of message in bits
        long bitLength = (count % b) << 3;
        if (bitLength != 0) {
            // append 0-bits to message
            for (int i = (int) count % b; i < b; i++) {
                buffer[i] = 0;
            }

            int t = (int) ((bitLength + k - 1) / k);
            processBlock(t);
        }
    }

    /**
     * Process t blocks of k bytes.
     */
    private synchronized void processBlock(int t) {
        int length = buffer.length << 3;
        FlexiBigInt[] x_ = new FlexiBigInt[t + 1];

        Bitstring m = new Bitstring(length, buffer);
        x_[0] = state;
        for (int j = 0; j < t; j++) {
            FlexiBigInt p = FlexiBigInt.ONE;
            for (int i = 0; i < k; i++) {
                p = p.multiply(
                        ((FlexiBigInt) v.elementAt(i + 1)).pow(m.getBit(length
                                - 1 - i - j * k))).mod(n);
            }
            x_[j + 1] = x_[j].multiply(x_[j]).mod(n).multiply(p).mod(n);
        }
        state = x_[t];
    }

}
