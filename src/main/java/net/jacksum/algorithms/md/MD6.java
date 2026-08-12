/* File:    MD6.java
** Author:  Java port of Ronald L. Rivest's MD6 reference implementation
**          (md6.h, md6_compress.c, md6_mode.c; MD6 submission to the
**          NIST SHA-3 competition, MIT, 2008, last revised 2009-01-15).
**
** (The following license is known as "The MIT License")
**
** Copyright (c) 2008 Ronald L. Rivest
**
** Permission is hereby granted, free of charge, to any person obtaining a copy
** of this software and associated documentation files (the "Software"), to deal
** in the Software without restriction, including without limitation the rights
** to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
** copies of the Software, and to permit persons to whom the Software is
** furnished to do so, subject to the following conditions:
**
** The above copyright notice and this permission notice shall be included in
** all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
** OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
** THE SOFTWARE.
**
** (end of license)
**
** See http://groups.csail.mit.edu/cis/md6 for more information.
*/

package net.jacksum.algorithms.md;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * The MD6 hash function as a {@link java.security.MessageDigest}.
 *
 * <p>This is a port of the standard (w = 64) version of MD6: the compression
 * function of {@code md6_compress.c} and the hierarchical mode of operation of
 * {@code md6_mode.c}. All structural constants are the ones of standard MD6;
 * the variant word sizes 8/16/32 of the C reference are not ported.
 *
 * <p>Both keyless and keyed MD6 are supported, as are the mode parameter
 * {@code L} (0 = purely sequential Merkle-Damg&aring;rd, &ge; 29 = purely
 * tree-based, default 64 = fully hierarchical) and the number of rounds
 * {@code r}.
 *
 * <p><b>Byte order.</b> MD6 words are big-endian by definition. The C
 * reference stores message bytes raw into an array of {@code md6_word} and
 * byte-swaps them on little-endian machines
 * ({@code md6_reverse_little_endian()}). This port instead keeps the tree
 * stack as bytes and loads words big-endian at compression time, which is
 * exactly equivalent and needs no endianness handling at all -- including in
 * SEQ mode, where the C code deliberately does <i>not</i> reverse the leading
 * chaining variable: a chaining value stored big-endian and read back
 * big-endian yields the same word.
 *
 * <p>Instances are not thread-safe (as is usual for {@code MessageDigest}).
 */
public class MD6 extends MessageDigest implements Cloneable {

    /* ---------------------------------------------------------------- */
    /* MD6 constants (md6.h)                                            */
    /* ---------------------------------------------------------------- */

    /** Number of bits in an MD6 word. */
    private static final int W = 64;
    /** Number of words in a compression input block. */
    private static final int N = 89;
    /** Number of words in a compression output ("chunk"). */
    private static final int C = 16;
    /** Number of data words per compression input block. */
    private static final int B = 64;
    /** Number of Q words per compression input block. */
    private static final int Q_WORDS = 15;
    /** Number of key words per compression input block. */
    private static final int K_WORDS = 8;

    /** Maximum allowable number of rounds. */
    public static final int MAX_R = 255;
    /** Maximum allowable digest length, in bits (= C * W / 2). */
    public static final int MAX_D = 512;
    /** Default mode parameter; large, so that MD6 is fully hierarchical. */
    public static final int DEFAULT_L = 64;
    /** Default digest length in bits, as used by the {@code md6sum} tool. */
    public static final int DEFAULT_D = 256;

    /** Maximum tree height; bounds the message length at 2^64 bits. */
    private static final int MAX_STACK_HEIGHT = 29;

    private static final int BLOCK_BYTES = B * W / 8;   /* 512 */
    private static final int BLOCK_BITS = B * W;        /* 4096 */
    private static final int CHUNK_BYTES = C * W / 8;   /* 128 */
    private static final int CHUNK_BITS = C * W;        /* 1024 */
    private static final int MAX_KEY_BYTES = K_WORDS * W / 8; /* 64 */

    /* ---------------------------------------------------------------- */
    /* Compression function constants (md6_compress.c, md6_mode.c)      */
    /* ---------------------------------------------------------------- */

    /** Q = initial 960 bits of the fractional part of sqrt(6). */
    private static final long[] Q = {
        0x7311c2812425cfa0L, 0x6432286434aac8e7L, 0xb60450e9ef68b7c1L,
        0xe8fb23908d9f06f1L, 0xdd2e76cba691e5bfL, 0x0cd0d63b2c30bc41L,
        0x1f8ccf6823058f8aL, 0x54e5ed5b88e3775dL, 0x4ad12aae0a6d6031L,
        0x3e7f16bb88222e0dL, 0x8af8671d3fb50c2cL, 0x995ad1178bd25c31L,
        0xc878c1dd04c4b633L, 0x3b72066c7a1552acL, 0x0d6f3522631effcbL,
    };

    /** Initial round constant. */
    private static final long S0 = 0x0123456789abcdefL;
    /** Mask used when advancing the round constant. */
    private static final long SMASK = 0x7311c2812425cfa0L;

    /* Tap positions for the feedback shift register (n == 89):
    **   t0 = 17  index for linear feedback
    **   t1 = 18  first input to first and
    **   t2 = 21  second input to first and
    **   t3 = 31  first input to second and
    **   t4 = 67  second input to second and
    **   t5 = 89  last tap (end-around feedback)
    ** They appear as literals in mainCompressionLoop(), together with the
    ** right/left shift amounts of RL00..RL15.
    */

    /* ---------------------------------------------------------------- */
    /* Parameters                                                       */
    /* ---------------------------------------------------------------- */

    private int d;              /* digest length in bits, 1 <= d <= 512   */
    private int r;              /* number of rounds, 0 <= r <= 255        */
    private int modeL;          /* mode parameter L, 0 <= L <= 255        */
    private int keylen;         /* key length in bytes, 0 <= keylen <= 64 */
    private long[] key;         /* key (aka salt), zero-padded to 8 words */
    private byte[] keyBytes;    /* the key as given (for clone/toString)  */

    /* ---------------------------------------------------------------- */
    /* State                                                            */
    /* ---------------------------------------------------------------- */

    /* Tree stack; stack[ell] holds the *inputs* of the node at level ell,
    ** as bytes (MD6 words, big-endian). Level 0 is unused, message bytes
    ** go into stack[1]. See the "Data structure notes" in md6_mode.c.
    */
    private byte[][] stack;
    private int[] bits;         /* bits already placed into stack[ell]    */
    private long[] iForLevel;   /* index of node stack[ell] on its level  */
    private int top;            /* largest ell that has received data     */

    private long bitsProcessed;
    private long compressionCalls;

    /** The untrimmed final chaining value (16 words, big-endian). */
    private byte[] finalCV;

    /* Scratch space, reused across compressions. The C reference uses a
    ** fixed A[5000] because MS Visual Studio could not handle a variable
    ** size one; r <= 255 gives r*C+N = 4169.
    */
    private long[] a;
    private long[] nBuf;
    private byte[] oneByte;

    /* ---------------------------------------------------------------- */
    /* Construction                                                     */
    /* ---------------------------------------------------------------- */

    /** Creates standard MD6 with a 256-bit digest. */
    public MD6() {
        this(DEFAULT_D);
    }

    /**
     * Creates standard (keyless, hierarchical) MD6.
     *
     * @param d digest length in bits, 1 &le; d &le; 512
     */
    public MD6(int d) {
        this(d, null, DEFAULT_L, defaultR(d, 0));
    }

    /**
     * Creates keyed MD6 with default mode parameter and default number of
     * rounds for the given digest length and key length.
     *
     * @param d   digest length in bits, 1 &le; d &le; 512
     * @param key key (aka salt), at most 64 bytes; may be {@code null}
     */
    public MD6(int d, byte[] key) {
        this(d, key, DEFAULT_L, defaultR(d, key == null ? 0 : key.length));
    }

    /**
     * Creates MD6 with all parameters specified.
     *
     * @param d   digest length in bits, 1 &le; d &le; 512
     * @param key key (aka salt), at most 64 bytes; may be {@code null}
     * @param L   mode parameter, 0 &le; L &le; 255; 0 selects purely
     *            sequential (Merkle-Damg&aring;rd) mode, {@link #DEFAULT_L}
     *            the standard hierarchical mode
     * @param r   number of rounds, 0 &le; r &le; 255
     */
    public MD6(int d, byte[] key, int L, int r) {
        super("MD6-" + d);
        if (d < 1 || d > MAX_D) {
            throw new IllegalArgumentException(
                "MD6: digest length d must be in 1..." + MAX_D + ", got " + d);
        }
        if (key != null && key.length > MAX_KEY_BYTES) {
            throw new IllegalArgumentException(
                "MD6: key length must be at most " + MAX_KEY_BYTES
                + " bytes, got " + key.length);
        }
        if (L < 0 || L > 255) {
            throw new IllegalArgumentException(
                "MD6: mode parameter L must be in 0..255, got " + L);
        }
        if (r < 0 || r > MAX_R) {
            throw new IllegalArgumentException(
                "MD6: number of rounds r must be in 0..." + MAX_R
                + ", got " + r);
        }
        this.d = d;
        this.modeL = L;
        this.r = r;
        this.keyBytes = (key == null || key.length == 0)
                ? new byte[0] : key.clone();
        this.keylen = this.keyBytes.length;
        /* The key is a k-word value; the first key byte goes into the
        ** high end of K[0]. This is what md6_full_init() achieves with
        ** memcpy() followed by md6_reverse_little_endian().
        */
        this.key = new long[K_WORDS];
        byte[] padded = Arrays.copyOf(this.keyBytes, MAX_KEY_BYTES);
        for (int i = 0; i < K_WORDS; i++) {
            this.key[i] = getLongBE(padded, 8 * i);
        }

        this.stack = new byte[MAX_STACK_HEIGHT][BLOCK_BYTES];
        this.bits = new int[MAX_STACK_HEIGHT];
        this.iForLevel = new long[MAX_STACK_HEIGHT];
        this.finalCV = new byte[CHUNK_BYTES];
        this.a = new long[MAX_R * C + N];
        this.nBuf = new long[N];
        this.oneByte = new byte[1];
        resetState();
    }

    /**
     * The default number of rounds: forty plus floor(d/4), but at least 80
     * if a key is used (see {@code md6_default_r()}).
     *
     * @param d      digest length in bits
     * @param keylen key length in bytes
     * @return the default number of rounds
     */
    public static int defaultR(int d, int keylen) {
        int r = 40 + (d / 4);
        if (keylen > 0) {
            r = Math.max(80, r);
        }
        return r;
    }

    /* ---------------------------------------------------------------- */
    /* Main compression loop (md6_main_compression_loop)                */
    /* ---------------------------------------------------------------- */

    /**
     * Performs the MD6 "main compression loop" on the array {@code a}: the
     * heart of MD6. The first {@code N} words of {@code a} must be set up;
     * words {@code a[N .. r*C+N-1]} are filled in.
     *
     * <p>The loop is unrolled C = 16 times (one round), with the tap
     * positions and the shift amounts of {@code RL00..RL15} as literals, the
     * way the C macros expand. All right shifts are logical, matching the
     * {@code uint64_t} arithmetic of the C reference.
     */
    static void mainCompressionLoop(long[] a, int r) {
        long s = S0;
        int i = N;
        for (int j = 0; j < r * C; j += C) {
            long x;
            /* x = S ^ end-around feedback ^ linear feedback
                 ^ first quadratic term ^ second quadratic term,
               then right-shift and left-shift. */
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 10; a[i] = x ^ (x << 11); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 5;  a[i] = x ^ (x << 24); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 13; a[i] = x ^ (x << 9);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 10; a[i] = x ^ (x << 16); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 11; a[i] = x ^ (x << 15); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 12; a[i] = x ^ (x << 9);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 2;  a[i] = x ^ (x << 27); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 7;  a[i] = x ^ (x << 15); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 14; a[i] = x ^ (x << 6);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 15; a[i] = x ^ (x << 2);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 7;  a[i] = x ^ (x << 29); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 13; a[i] = x ^ (x << 8);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 11; a[i] = x ^ (x << 15); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 7;  a[i] = x ^ (x << 5);  i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 6;  a[i] = x ^ (x << 31); i++;
            x = s ^ a[i - 89] ^ a[i - 17] ^ (a[i - 18] & a[i - 21]) ^ (a[i - 31] & a[i - 67]);
            x ^= x >>> 12; a[i] = x ^ (x << 9);  i++;

            /* Advance the round constant to the next round constant. */
            s = (s << 1) ^ (s >>> (W - 1)) ^ (s & SMASK);
        }
    }

    /**
     * "Bare" compression routine: compresses the N-word input {@code n} to
     * the C-word output {@code out} (see {@code md6_compress()}).
     *
     * <p>Unlike the C version, the working array is not zeroed first: every
     * word of {@code a[N .. r*C+N-1]} is written before it is read.
     */
    private void compress(long[] out, long[] n, int r) {
        System.arraycopy(n, 0, a, 0, N);
        mainCompressionLoop(a, r);
        System.arraycopy(a, (r - 1) * C + N, out, 0, C);
    }

    /**
     * Constructs the control word V for the given inputs
     * (see {@code md6_make_control_word()}).
     */
    static long makeControlWord(int r, int L, int z, int p, int keylen, int d) {
        return (((long) 0) << 60)                /* reserved, width  4 bits */
             | (((long) r) << 48)                /*            width 12 bits */
             | (((long) L) << 40)                /*            width  8 bits */
             | (((long) z) << 36)                /*            width  4 bits */
             | (((long) p) << 20)                /*            width 16 bits */
             | (((long) keylen) << 12)           /*            width  8 bits */
             | ((long) d);                       /*            width 12 bits */
    }

    /**
     * Constructs the unique node ID U from the level number and the index
     * within that level (see {@code md6_make_nodeID()}).
     *
     * <p>Note: the C reference narrows the index to {@code int} here, since
     * {@code md6_make_nodeID()} takes an {@code int}. That only makes a
     * difference for messages with at least 2^31 leaf blocks (about 1 TiB);
     * this port follows the specification and uses the full 56-bit index.
     */
    static long makeNodeID(int ell, long i) {
        return (((long) ell) << 56) | i;
    }

    /**
     * Packs the components of a compression input into the N-word array
     * {@code n}: Q (words 0..14), K (15..22), U (23), V (24) and the data
     * block B (25..88), which is read big-endian from {@code data}
     * (see {@code md6_pack()}).
     */
    private void pack(long[] n, int ell, long index, int z, int p, byte[] data) {
        int ni = 0;
        for (int j = 0; j < Q_WORDS; j++) {
            n[ni++] = Q[j];
        }
        for (int j = 0; j < K_WORDS; j++) {
            n[ni++] = key[j];
        }
        n[ni++] = makeNodeID(ell, index);
        n[ni++] = makeControlWord(r, modeL, z, p, keylen, d);
        for (int j = 0; j < B; j++) {
            n[ni++] = getLongBE(data, 8 * j);
        }
    }

    /**
     * Performs an MD6 block compression using all the "standard" inputs
     * (see {@code md6_standard_compress()}).
     */
    private void standardCompress(long[] out, int ell, long index,
                                  int z, int p, byte[] data) {
        if (ell < 0 || ell > 255) {
            throw new IllegalStateException("MD6: level number out of range: " + ell);
        }
        if (p < 0 || p > BLOCK_BITS) {
            throw new IllegalStateException("MD6: number of pad bits out of range: " + p);
        }
        pack(nBuf, ell, index, z, p, data);
        compress(out, nBuf, r);
    }

    /* ---------------------------------------------------------------- */
    /* Mode of operation (md6_mode.c)                                   */
    /* ---------------------------------------------------------------- */

    /**
     * Compresses the block at level {@code ell} into {@code out}, then clears
     * that level and advances its node index
     * (see {@code md6_compress_block()}).
     *
     * @param z 1 iff this is the very last compression
     */
    private void compressBlock(long[] out, int ell, int z) {
        if (ell < 1) {
            throw new IllegalStateException("MD6: stack underflow");
        }
        if (ell >= MAX_STACK_HEIGHT - 1) {
            throw new IllegalStateException("MD6: stack overflow (message too long)");
        }
        compressionCalls++;

        /* No byte reversal needed here: words in stack[] are already stored
        ** big-endian, which is what pack() expects. This covers both the PAR
        ** case (md6_reverse_little_endian of all b words) and the SEQ case
        ** (all but the leading c chaining words) of md6_compress_block().
        */
        int p = BLOCK_BITS - bits[ell];     /* number of pad bits */
        standardCompress(out, ell, iForLevel[ell], z, p, stack[ell]);

        bits[ell] = 0;
        iForLevel[ell]++;
        Arrays.fill(stack[ell], (byte) 0);
    }

    /**
     * Processes (compresses) the block at level {@code ell} and, recursively,
     * its now-compressible ancestors (see {@code md6_process()}). On the very
     * last compression the result is kept in {@link #finalCV}.
     *
     * @param isFinal true iff called while finishing up the hash computation
     */
    private void process(int ell, boolean isFinal) {
        if (!isFinal) {
            /* not final: nothing to do unless the block on this level is full */
            if (bits[ell] < BLOCK_BITS) {
                return;
            }
        } else {
            if (ell == top) {
                if (ell == modeL + 1) {          /* SEQ node */
                    if (bits[ell] == CHUNK_BITS && iForLevel[ell] > 0) {
                        return;
                    }
                } else {                        /* top tree node */
                    if (ell > 1 && bits[ell] == CHUNK_BITS) {
                        return;
                    }
                }
            }
        }

        /* z = 1 iff this is the very last compression */
        int z = (isFinal && ell == top) ? 1 : 0;
        long[] out = new long[C];
        compressBlock(out, ell, z);

        if (z == 1) {                           /* save final chaining value */
            for (int i = 0; i < C; i++) {
                putLongBE(out[i], finalCV, 8 * i);
            }
            return;
        }

        int nextLevel = Math.min(ell + 1, modeL + 1);
        /* Start sequential mode with IV = 0 at that level if necessary; the
        ** bits themselves are already zero, so only bits[] has to be set.
        */
        if (nextLevel == modeL + 1
                && iForLevel[nextLevel] == 0
                && bits[nextLevel] == 0) {
            bits[nextLevel] = CHUNK_BITS;
        }
        int off = bits[nextLevel] / 8;
        for (int i = 0; i < C; i++) {
            putLongBE(out[i], stack[nextLevel], off + 8 * i);
        }
        bits[nextLevel] += CHUNK_BITS;
        if (nextLevel > top) {
            top = nextLevel;
        }
        process(nextLevel, isFinal);
    }

    /**
     * Incorporates {@code bitLen} bits of {@code data} into the hash
     * computation (see {@code md6_update()}). Unlike
     * {@link #update(byte[], int, int)} this allows message lengths that are
     * not a multiple of eight bits; the high-order bits of a byte are used
     * first.
     *
     * <p>The result only depends on the concatenation of all the bits fed in,
     * not on how they are split across calls. Note that the C reference
     * behaves differently in one case: when a call whose data does not start
     * at a byte boundary of the accumulated message is long enough to be split
     * at a level-1 block boundary, {@code md6_update()} passes
     * {@code &data[j/8]} to {@code append_bits()} and thereby loses the bit
     * offset {@code j % 8}, re-using up to seven bits it has already
     * consumed. Byte-aligned use -- everything reachable through
     * {@code MessageDigest} -- is unaffected.
     *
     * @param data   the data to hash
     * @param bitLen its length in bits, at most {@code 8 * data.length}
     */
    public void updateBits(byte[] data, long bitLen) {
        updateBits(data, 0, bitLen);
    }

    /**
     * Incorporates {@code bitLen} bits of {@code data}, starting at byte
     * offset {@code off}, into the hash computation.
     *
     * @param data   the data to hash
     * @param off    offset of the first byte to use
     * @param bitLen number of bits to use, at most {@code 8*(data.length-off)}
     */
    public void updateBits(byte[] data, int off, long bitLen) {
        if (data == null) {
            throw new NullPointerException("MD6: data is null");
        }
        if (bitLen < 0) {
            throw new IllegalArgumentException("MD6: negative bit length " + bitLen);
        }
        if (off < 0 || bitLen > 8L * (data.length - off)) {
            throw new IndexOutOfBoundsException(
                "MD6: " + bitLen + " bits do not fit into "
                + (data.length - off) + " bytes at offset " + off);
        }

        long j = 0;    /* number of bits processed so far in this call */
        while (j < bitLen) {
            /* Handle the input in portions; portion may be zero if the
            ** level-1 block is already full from a previous call.
            */
            int portion = (int) Math.min(bitLen - j, BLOCK_BITS - bits[1]);

            if ((portion % 8 == 0) && (bits[1] % 8 == 0) && (j % 8 == 0)) {
                /* easy, but most common, case */
                System.arraycopy(data, off + (int) (j / 8),
                                 stack[1], bits[1] / 8, portion / 8);
            } else {
                appendBits(stack[1], bits[1], data, 8L * off + j, portion);
            }
            j += portion;
            bits[1] += portion;
            bitsProcessed += portion;

            /* compress the level-1 block if it is now full,
            ** but we are not done yet */
            if (bits[1] == BLOCK_BITS && j < bitLen) {
                process(1, false);
            }
        }
    }

    /**
     * Appends the bit string of {@code srcLen} bits that starts at bit
     * {@code srcBitOff} of {@code src} to the end of the bit string
     * {@code dest} of {@code destLen} bits (see {@code append_bits()}). In
     * both strings the high-order bits of a byte come first; unused bit
     * positions of the last byte are zeroed.
     *
     * <p>The C reference takes a byte-aligned source only; the bit offset is
     * this port's addition, see {@link #updateBits(byte[], int, long)}.
     */
    private static void appendBits(byte[] dest, int destLen,
                                   byte[] src, long srcBitOff, int srcLen) {
        if (srcLen == 0) {
            return;
        }
        /* accum accumulates bits waiting to be moved, right-justified;
        ** it is a uint16_t in the C reference, hence the 0xffff masks.
        */
        int accum = 0;
        int accumLen = 0;
        if (destLen % 8 != 0) {
            accumLen = destLen % 8;
            accum = (dest[destLen / 8] & 0xff) >>> (8 - accumLen);
        }
        int di = destLen / 8;    /* where the next byte goes within dest */

        int srcBytes = (srcLen + 7) / 8;
        for (int i = 0; i < srcBytes; i++) {
            /* shift the good bits from src[i] into accum */
            if (i != srcBytes - 1) {                     /* not last byte */
                accum = ((accum << 8) ^ srcByte(src, srcBitOff, i)) & 0xffff;
                accumLen += 8;
            } else {                                     /* last byte */
                int newBits = (srcLen % 8 == 0) ? 8 : (srcLen % 8);
                accum = ((accum << newBits)
                        | (srcByte(src, srcBitOff, i) >>> (8 - newBits))) & 0xffff;
                accumLen += newBits;
            }
            /* do as many high-order bits of accum as you can (or need to) */
            while (((i != srcBytes - 1) && (accumLen >= 8))
                    || ((i == srcBytes - 1) && (accumLen > 0))) {
                int numBits = Math.min(8, accumLen);
                int out = accum >>> (accumLen - numBits);   /* right justified */
                out = out << (8 - numBits);                 /* left justified  */
                out &= (0xff00 >> numBits);                 /* mask            */
                dest[di++] = (byte) out;                    /* save            */
                accumLen -= numBits;
            }
        }
    }

    /**
     * Returns byte {@code i} of the bit string that starts at bit
     * {@code srcBitOff} of {@code src}. Bits beyond the end of {@code src}
     * read as zero; they are always beyond the requested bit string, too.
     */
    private static int srcByte(byte[] src, long srcBitOff, int i) {
        int index = (int) (srcBitOff / 8) + i;
        int shift = (int) (srcBitOff % 8);
        int value = (src[index] & 0xff) << shift;
        if (shift != 0 && index + 1 < src.length) {
            value |= (src[index + 1] & 0xff) >>> (8 - shift);
        }
        return value & 0xff;
    }

    /**
     * Finishes the tree and leaves the final chaining value in
     * {@link #finalCV} (see the first half of {@code md6_final()}).
     */
    private void finishTree() {
        /* force any processing that needs doing */
        int ell;
        if (top == 1) {
            ell = 1;
        } else {
            for (ell = 1; ell <= top; ell++) {
                if (bits[ell] > 0) {
                    break;
                }
            }
        }
        process(ell, true);
    }

    /**
     * Extracts the last {@code d} bits of the final chaining value as the
     * hash value (see {@code trim_hashval()}). The high-order bit of a byte
     * counts as its first bit.
     */
    private static byte[] trim(byte[] cv, int d) {
        int nbytes = (d + 7) / 8;              /* full or partial bytes */
        int bits = d % 8;                      /* bits in partial byte  */
        byte[] h = cv.clone();

        /* move the relevant bytes to the front */
        for (int i = 0; i < nbytes; i++) {
            h[i] = h[CHUNK_BYTES - nbytes + i];
        }
        for (int i = nbytes; i < CHUNK_BYTES; i++) {
            h[i] = 0;
        }
        /* shift the result left by (8-bits) bit positions, per byte, if needed */
        if (bits > 0) {
            for (int i = 0; i < nbytes; i++) {
                h[i] = (byte) (h[i] << (8 - bits));
                if (i + 1 < CHUNK_BYTES) {
                    h[i] |= (byte) ((h[i + 1] & 0xff) >>> bits);
                }
            }
        }
        return Arrays.copyOf(h, nbytes);
    }

    /* ---------------------------------------------------------------- */
    /* MessageDigest SPI                                                */
    /* ---------------------------------------------------------------- */

    @Override
    protected void engineUpdate(byte input) {
        oneByte[0] = input;
        updateBits(oneByte, 0, 8);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        updateBits(input, offset, 8L * len);
    }

    @Override
    protected byte[] engineDigest() {
        finishTree();
        byte[] hashval = trim(finalCV, d);
        engineReset();
        return hashval;
    }

    @Override
    protected int engineGetDigestLength() {
        return (d + 7) / 8;
    }

    @Override
    protected void engineReset() {
        resetState();
    }

    /** Clears the tree state, keeping the parameters d, key, L and r. */
    private void resetState() {
        for (int i = 0; i < MAX_STACK_HEIGHT; i++) {
            Arrays.fill(stack[i], (byte) 0);
        }
        Arrays.fill(bits, 0);
        Arrays.fill(iForLevel, 0L);
        top = 1;
        bitsProcessed = 0;
        compressionCalls = 0;
        /* If SEQ mode applies to level 1, use IV = 0: the bits are already
        ** zero, so only bits[1] has to be set. */
        if (modeL == 0) {
            bits[1] = CHUNK_BITS;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MD6 copy = (MD6) super.clone();
        copy.key = key.clone();
        copy.keyBytes = keyBytes.clone();
        copy.bits = bits.clone();
        copy.iForLevel = iForLevel.clone();
        copy.finalCV = finalCV.clone();
        copy.stack = new byte[MAX_STACK_HEIGHT][];
        for (int i = 0; i < MAX_STACK_HEIGHT; i++) {
            copy.stack[i] = stack[i].clone();
        }
        copy.a = new long[a.length];
        copy.nBuf = new long[N];
        copy.oneByte = new byte[1];
        return copy;
    }

    /* ---------------------------------------------------------------- */
    /* Extras                                                           */
    /* ---------------------------------------------------------------- */

    /**
     * Completes the hash computation like {@link #digest()}, but returns the
     * <i>untrimmed</i> final chaining value: all 16 output words (128 bytes)
     * of the last compression, big-endian. The digest proper is the last
     * {@code d} bits of this value. Like {@code digest()}, this resets the
     * digest.
     *
     * <p>This is the value that {@code md6_final()} of the C reference copies
     * into the caller's buffer (before trimming), and hence what
     * {@code genKAT.c} writes into the {@code *KAT_*.txt} files.
     *
     * @return the 128-byte final chaining value
     */
    public byte[] getFinalChainingValue() {
        finishTree();
        byte[] cv = finalCV.clone();
        engineReset();
        return cv;
    }

    /** @return the digest length in bits (d). */
    public int getD() {
        return d;
    }

    /** @return the number of rounds (r). */
    public int getR() {
        return r;
    }

    /** @return the mode parameter (L). */
    public int getL() {
        return modeL;
    }

    /** @return the key length in bytes. */
    public int getKeyLength() {
        return keylen;
    }

    /** @return the number of bits fed into the current computation. */
    public long getBitsProcessed() {
        return bitsProcessed;
    }

    /** @return the number of compression function calls made so far. */
    public long getCompressionCalls() {
        return compressionCalls;
    }

    @Override
    public String toString() {
        return "MD6[d=" + d + ", L=" + modeL + ", r=" + r
                + ", keylen=" + keylen + "]";
    }

    /* ---------------------------------------------------------------- */
    /* Word access (MD6 words are big-endian by definition)             */
    /* ---------------------------------------------------------------- */

    private static long getLongBE(byte[] src, int off) {
        return  ((long) (src[off    ] & 0xff) << 56)
              | ((long) (src[off + 1] & 0xff) << 48)
              | ((long) (src[off + 2] & 0xff) << 40)
              | ((long) (src[off + 3] & 0xff) << 32)
              | ((long) (src[off + 4] & 0xff) << 24)
              | ((long) (src[off + 5] & 0xff) << 16)
              | ((long) (src[off + 6] & 0xff) << 8)
              |  (long) (src[off + 7] & 0xff);
    }

    private static void putLongBE(long value, byte[] dest, int off) {
        dest[off    ] = (byte) (value >>> 56);
        dest[off + 1] = (byte) (value >>> 48);
        dest[off + 2] = (byte) (value >>> 40);
        dest[off + 3] = (byte) (value >>> 32);
        dest[off + 4] = (byte) (value >>> 24);
        dest[off + 5] = (byte) (value >>> 16);
        dest[off + 6] = (byte) (value >>> 8);
        dest[off + 7] = (byte) value;
    }

    /* ---------------------------------------------------------------- */
    /* Named variants, for instantiation through a JCA provider          */
    /* ---------------------------------------------------------------- */

    /** Standard MD6 with a 224-bit digest. */
    public static final class MD6_224 extends MD6 {
        public MD6_224() {
            super(224);
        }
    }

    /** Standard MD6 with a 256-bit digest. */
    public static final class MD6_256 extends MD6 {
        public MD6_256() {
            super(256);
        }
    }

    /** Standard MD6 with a 384-bit digest. */
    public static final class MD6_384 extends MD6 {
        public MD6_384() {
            super(384);
        }
    }

    /** Standard MD6 with a 512-bit digest. */
    public static final class MD6_512 extends MD6 {
        public MD6_512() {
            super(512);
        }
    }
}

/* end of MD6.java */
