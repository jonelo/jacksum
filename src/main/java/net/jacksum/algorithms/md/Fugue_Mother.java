/*

  Jacksum 4.0.0 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, <https://jacksum.net>.

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <https://www.gnu.org/licenses/>.

*/

package net.jacksum.algorithms.md;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Common base class of all Fugue and Fugue&nbsp;2.0 message digests.
 *
 * <p>Fugue is a cryptographic hash function submitted by IBM (Shai Halevi, William E. Hall,
 * Charanjit S. Jutla) to the NIST SHA-3 hash function competition. Fugue&nbsp;2.0 is a tweak of
 * the original design that needs fewer SMIX applications per input word and therefore runs
 * roughly twice as fast for a 256-bit output.</p>
 *
 * <p>Both are instances of one parameterized design {@code F[n,s,k,r,t]}:</p>
 * <ul>
 *   <li>{@code n} &ndash; output size in four-byte words (which is also the number of IV words)</li>
 *   <li>{@code s} &ndash; state size in four-byte columns</li>
 *   <li>{@code k} &ndash; number of sub-rounds per input word</li>
 *   <li>{@code r} &ndash; number of TIX-less rounds in the first phase (G1) of the final round</li>
 *   <li>{@code t} &ndash; number of rounds in the second phase (G2) of the final round</li>
 * </ul>
 *
 * <p>The two families differ only in their IVs, their {@code TIX} step and their parameters;
 * everything else &ndash; {@code SMIX}, {@code CMIX}, {@code RORn}, the final round {@code G2},
 * the output selection and the padding &ndash; is shared and implemented here.</p>
 *
 * <h2>Byte and word conventions</h2>
 * <p>A state column is a four-byte word, and row <i>r</i> of a column is the <i>r</i>-th byte
 * of the byte stream. A column is therefore held in an {@code int} in big-endian packing:
 * {@code row(r) = (column >>> (24 - 8 * r)) & 0xFF}. Message words are big-endian, the digest
 * consists of the big-endian bytes of the selected output columns, and the appended message
 * length is the big-endian 64-bit <em>bit</em> count.</p>
 *
 * <h2>Thread safety</h2>
 * <p>Instances are not thread safe, like every other {@link MessageDigest}.</p>
 *
 * @see Fugue_224
 * @see Fugue_256
 * @see Fugue_384
 * @see Fugue_512
 * @see Fugue2_224
 * @see Fugue2_256
 * @see Fugue2_384
 * @see Fugue2_512
 */
public abstract class Fugue_Mother extends MessageDigest implements Cloneable {

    // ------------------------------------------------------------------------------------
    // TIX variants
    // ------------------------------------------------------------------------------------

    /**
     * The generic {@code TIX} step of the original Fugue, for all of 224/256/384/512:
     * {@code S[6k-2] += S0; S0 = P; S8 += S0; for i = 0..k-2: S[3i+1] += S[s-3k+3i]}.
     */
    protected static final int TIX_FUGUE = 1;

    /**
     * The {@code TIX[30,1]} step of Fugue&nbsp;2.0, used by Fugue2-224 and Fugue2-256:
     * {@code S4 += S0; S0 = P; S14 += S0; S20 += S0; S8 += S1}.
     */
    protected static final int TIX_FUGUE2_30_1 = 2;

    /**
     * The {@code TIX[36,2]} step of Fugue&nbsp;2.0, used by Fugue2-384:
     * {@code S7 += S0; S0 = P; S10 += S0; S14 += S0; S4 += S1}.
     */
    protected static final int TIX_FUGUE2_36_2 = 3;

    /**
     * The {@code TIX[36,3]} step of Fugue&nbsp;2.0, used by Fugue2-512:
     * {@code S10 += S0; S0 = P; S7 += S0; S11 += S0; S4 += S1; S22 += S1}.
     */
    protected static final int TIX_FUGUE2_36_3 = 4;

    // ------------------------------------------------------------------------------------
    // Shared tables
    // ------------------------------------------------------------------------------------

    /** Reduction polynomial of GF(2^8): x^8 + x^4 + x^3 + x + 1. */
    private static final int POLY = 0x11B;

    /**
     * The circulant Super-Mix matrix M of Fugue, {@code M[row][column]}. Note that this is
     * not the AES column mix matrix.
     */
    private static final int[][] M = {
            {1, 4, 7, 1},
            {1, 1, 4, 7},
            {7, 1, 1, 4},
            {4, 7, 1, 1},
    };

    /** The AES substitution box, which Fugue uses unchanged. */
    private static final int[] SBOX = new int[256];

    /**
     * Super-Mix lookup tables. {@code SMIX_TABLE[k][x]} holds, in big-endian packing, the four
     * bytes {@code M[r][k] * SBOX[x]} for {@code r = 0..3}, i.e. column {@code k} of M scaled
     * by the substituted byte {@code x}.
     */
    private static final int[][] SMIX_TABLE = new int[4][256];

    static {
        buildSbox();
        buildSmixTables();
    }

    /** Builds the AES S-box from the multiplicative inverse followed by the affine map. */
    private static void buildSbox() {
        // Multiplicative inverses in GF(2^8).
        final int[] inverse = new int[256];
        inverse[0] = 0;
        inverse[1] = 1;
        for (int i = 2; i < 256; i++) {
            for (int j = 2; j < 256; j++) {
                if (gfMul(i, j) == 1) {
                    inverse[i] = j;
                    break;
                }
            }
        }
        // Affine transformation: b ^ rotl(b,1) ^ rotl(b,2) ^ rotl(b,3) ^ rotl(b,4) ^ 0x63.
        for (int i = 0; i < 256; i++) {
            final int b = inverse[i];
            int x = b ^ 0x63;
            for (int shift = 1; shift <= 4; shift++) {
                x ^= ((b << shift) | (b >>> (8 - shift))) & 0xFF;
            }
            SBOX[i] = x & 0xFF;
        }
    }

    private static void buildSmixTables() {
        for (int k = 0; k < 4; k++) {
            for (int x = 0; x < 256; x++) {
                int word = 0;
                for (int r = 0; r < 4; r++) {
                    word |= gfMul(M[r][k], SBOX[x]) << (24 - 8 * r);
                }
                SMIX_TABLE[k][x] = word;
            }
        }
    }

    /** Multiplication in GF(2^8) modulo {@link #POLY}. */
    private static int gfMul(int a, int b) {
        int result = 0;
        int x = a;
        int y = b;
        while (y != 0) {
            if ((y & 1) != 0) {
                result ^= x;
            }
            y >>>= 1;
            x <<= 1;
            if ((x & 0x100) != 0) {
                x ^= POLY;
            }
        }
        return result & 0xFF;
    }

    // ------------------------------------------------------------------------------------
    // Instance parameters and state
    // ------------------------------------------------------------------------------------

    /** Output size in four-byte words. */
    private final int n;
    /** State size in four-byte columns. */
    private final int s;
    /** Sub-rounds per input word. */
    private final int k;
    /** TIX-less rounds in G1. */
    private final int r;
    /** Rounds in G2. */
    private final int t;
    /** One of the {@code TIX_*} constants. */
    private final int tixVariant;
    /** Number of output column groups, {@code ceil(n / 4)}. */
    private final int groups;
    /** Distance between output column groups, {@code s / groups}. */
    private final int groupStride;

    /** The initial vector, {@code n} words. */
    private int[] iv;

    /** The state, {@code s} columns; logical column {@code c} lives at {@code (base + c) % s}. */
    private int[] state;
    /** Rotation offset of the state. */
    private int base;
    /** Number of message bits fed in so far, excluding padding and length encoding. */
    private long totalBits;
    /** Buffer for the bytes of the current, incomplete input word. */
    private byte[] buffer = new byte[4];
    /** Set once a non byte-aligned update has been made; no further update is then allowed. */
    private boolean unaligned;

    /** Scratch space of {@link #smix()}, reused to avoid allocation in the hot path. */
    private int[] smixIndex = new int[4];
    private int[] smixColumn = new int[4];
    private int[] smixRow = new int[4];

    /**
     * Creates a digest with the given parameters.
     *
     * @param algorithm  the JCA algorithm name, e.g. {@code "Fugue-256"}
     * @param n          output size in four-byte words
     * @param s          state size in four-byte columns
     * @param k          number of sub-rounds per input word
     * @param r          number of TIX-less rounds in G1
     * @param t          number of rounds in G2
     * @param iv         the initial vector, {@code n} words; not modified, but retained
     * @param tixVariant one of {@link #TIX_FUGUE}, {@link #TIX_FUGUE2_30_1},
     *                   {@link #TIX_FUGUE2_36_2}, {@link #TIX_FUGUE2_36_3}
     */
    protected Fugue_Mother(String algorithm, int n, int s, int k, int r, int t, int[] iv, int tixVariant) {
        super(algorithm);
        if (iv.length != n) {
            throw new IllegalArgumentException("IV must consist of " + n + " words, got " + iv.length);
        }
        this.n = n;
        this.s = s;
        this.k = k;
        this.r = r;
        this.t = t;
        this.tixVariant = tixVariant;
        this.groups = (n + 3) / 4;
        this.groupStride = s / this.groups;
        this.iv = iv.clone();
        this.state = new int[s];
        engineReset();
    }

    // ------------------------------------------------------------------------------------
    // MessageDigest SPI
    // ------------------------------------------------------------------------------------

    @Override
    protected int engineGetDigestLength() {
        return n * 4;
    }

    @Override
    protected void engineReset() {
        Arrays.fill(state, 0);
        System.arraycopy(iv, 0, state, s - n, n);
        base = 0;
        totalBits = 0;
        unaligned = false;
        Arrays.fill(buffer, (byte) 0);
    }

    @Override
    protected void engineUpdate(byte input) {
        requireAligned();
        buffer[bufferedBytes()] = input;
        totalBits += 8;
        if ((totalBits & 31) == 0) {
            round(bigEndianWord(buffer, 0));
        }
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int length) {
        requireAligned();
        if (offset < 0 || length < 0 || offset + length > input.length) {
            throw new IllegalArgumentException("offset/length out of bounds");
        }
        int position = offset;
        int remaining = length;

        // Complete the currently buffered word first.
        int buffered = bufferedBytes();
        if (buffered != 0) {
            final int missing = Math.min(4 - buffered, remaining);
            System.arraycopy(input, position, buffer, buffered, missing);
            position += missing;
            remaining -= missing;
            totalBits += 8L * missing;
            if (buffered + missing == 4) {
                round(bigEndianWord(buffer, 0));
            }
        }

        // Consume whole words directly from the caller's array.
        while (remaining >= 4) {
            round(bigEndianWord(input, position));
            position += 4;
            remaining -= 4;
            totalBits += 32;
        }

        // Keep the tail for the next update or for the final padding.
        if (remaining != 0) {
            System.arraycopy(input, position, buffer, 0, remaining);
            totalBits += 8L * remaining;
        }
    }

    @Override
    protected byte[] engineDigest() {
        // Zero-pad the last, incomplete word. A trailing partial byte occupies a whole byte of
        // the buffer, hence the rounding up.
        if ((totalBits & 31) != 0) {
            for (int i = (int) (((totalBits & 31) + 7) >>> 3); i < 4; i++) {
                buffer[i] = 0;
            }
            round(bigEndianWord(buffer, 0));
        }
        // Append the message length in bits as two big-endian words.
        final long messageBits = totalBits;
        round((int) (messageBits >>> 32));
        round((int) messageBits);

        finalRounds();
        final byte[] digest = outputBytes();
        engineReset();
        return digest;
    }

    @Override
    public Fugue_Mother clone() {
        try {
            final Fugue_Mother copy = (Fugue_Mother) super.clone();
            copy.state = state.clone();
            copy.buffer = buffer.clone();
            copy.smixIndex = new int[4];
            copy.smixColumn = new int[4];
            copy.smixRow = new int[4];
            // iv is only ever replaced as a whole, never modified in place, so sharing is safe.
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    // ------------------------------------------------------------------------------------
    // Extensions beyond the MessageDigest contract
    // ------------------------------------------------------------------------------------

    /**
     * Feeds a bit string whose length is not necessarily a multiple of eight. Fugue is defined
     * for inputs of an arbitrary number of bits, whereas {@link MessageDigest} can only
     * express whole bytes; this method makes the remaining test vectors of the NIST
     * Known-Answer-Test files reachable, and it is the only way to reproduce a digest of, say,
     * a three-bit message.
     *
     * <p>As {@code bitLength} bits do not end on a byte boundary in general, this may only be
     * the <em>last</em> update before {@link #digest()}; any further update throws an
     * {@link IllegalStateException}. Following the NIST convention, the bits of the trailing
     * partial byte are taken from the most significant end, and the unused least significant
     * bits are ignored (this implementation masks them to zero, as the specification
     * prescribes; the C reference implementation leaves them untouched, which only makes a
     * difference for input that violates the convention).</p>
     *
     * @param input     the input bytes; {@code ceil(bitLength / 8)} bytes are read
     * @param offset    index of the first byte to read
     * @param bitLength number of bits to feed
     * @throws IllegalStateException if a non byte-aligned update has already been made
     */
    public void updateBits(byte[] input, int offset, long bitLength) {
        requireAligned();
        if (bitLength < 0) {
            throw new IllegalArgumentException("negative bitLength: " + bitLength);
        }
        final long fullBytes = bitLength >>> 3;
        final int trailingBits = (int) (bitLength & 7);
        final long neededBytes = fullBytes + (trailingBits != 0 ? 1 : 0);
        if (offset < 0 || neededBytes > input.length - offset) {
            throw new IllegalArgumentException(
                    bitLength + " bits need " + neededBytes + " bytes, but only "
                            + (input.length - Math.max(offset, 0)) + " are available");
        }
        if (fullBytes != 0) {
            update(input, offset, (int) fullBytes);
        }
        if (trailingBits != 0) {
            final int mask = (0xFF << (8 - trailingBits)) & 0xFF;
            buffer[bufferedBytes()] = (byte) (input[offset + (int) fullBytes] & mask);
            totalBits += trailingBits;
            unaligned = true;
        }
    }

    /**
     * Applies the bare compression function {@code F[n,s,k,r,t]} to the given words, without
     * any padding and without appending the message length, and returns the {@code n} output
     * words. This is how the fixed IVs of Fugue and Fugue&nbsp;2.0 are defined: the IV for an
     * <i>x</i>-bit digest is the result of running the algorithm with an all-zero IV on the
     * single word <i>x</i> (see the Fugue&nbsp;2.0 specification, section 5.3).
     *
     * <p>The digest is reset before and after the call.</p>
     *
     * @param words the input words
     * @return the {@code n} output words
     */
    public int[] rawWords(int... words) {
        engineReset();
        for (final int word : words) {
            round(word);
        }
        finalRounds();
        final int[] output = outputWords();
        engineReset();
        return output;
    }

    /**
     * Replaces the initial vector and resets the digest. With a 4<i>n</i>-byte key as the IV,
     * this turns the digest into the pseudo-random function PR-Fugue of the specification; it
     * is also how the fixed IVs are recomputed from the algorithm itself.
     *
     * @param newIv the new initial vector, {@code n} words
     */
    protected void setIv(int[] newIv) {
        if (newIv.length != n) {
            throw new IllegalArgumentException("IV must consist of " + n + " words, got " + newIv.length);
        }
        this.iv = newIv.clone();
        engineReset();
    }

    /** Returns the number of four-byte words in the digest. */
    public int getWordCount() {
        return n;
    }

    // ------------------------------------------------------------------------------------
    // The hash function itself
    // ------------------------------------------------------------------------------------

    /** Maps a logical column index to its index in {@link #state}. */
    private int column(int c) {
        final int x = base + c;
        return x < s ? x : x - s;
    }

    /** {@code RORn}: rotates the state right by {@code count} columns. */
    private void ror(int count) {
        base = column(s - count);
    }

    /** One round: {@code TIX(P)} followed by {@code k} sub-rounds. */
    private void round(int word) {
        tix(word);
        for (int i = 0; i < k; i++) {
            subRound();
        }
    }

    /** One sub-round: {@code ROR3; CMIX; SMIX}. */
    private void subRound() {
        ror(3);
        cmix();
        smix();
    }

    /** The {@code TIX} step; the variant is selected by the concrete algorithm. */
    private void tix(int word) {
        switch (tixVariant) {
            case TIX_FUGUE -> {
                state[column(6 * k - 2)] ^= state[column(0)];
                state[column(0)] = word;
                state[column(8)] ^= word;
                for (int i = 0; i <= k - 2; i++) {
                    state[column(3 * i + 1)] ^= state[column(s - 3 * k + 3 * i)];
                }
            }
            case TIX_FUGUE2_30_1 -> {
                state[column(4)] ^= state[column(0)];
                state[column(0)] = word;
                state[column(14)] ^= word;
                state[column(20)] ^= word;
                state[column(8)] ^= state[column(1)];
            }
            case TIX_FUGUE2_36_2 -> {
                state[column(7)] ^= state[column(0)];
                state[column(0)] = word;
                state[column(10)] ^= word;
                state[column(14)] ^= word;
                state[column(4)] ^= state[column(1)];
            }
            case TIX_FUGUE2_36_3 -> {
                state[column(10)] ^= state[column(0)];
                state[column(0)] = word;
                state[column(7)] ^= word;
                state[column(11)] ^= word;
                state[column(4)] ^= state[column(1)];
                state[column(22)] ^= state[column(1)];
            }
            default -> throw new IllegalStateException("unknown TIX variant: " + tixVariant);
        }
    }

    /**
     * {@code CMIX[s]}: {@code S0 += S4; S1 += S5; S2 += S6;} and the same into the columns
     * {@code s/2 .. s/2+2}.
     */
    private void cmix() {
        final int half = s / 2;
        for (int i = 0; i < 3; i++) {
            final int source = state[column(4 + i)];
            state[column(i)] ^= source;
            state[column(half + i)] ^= source;
        }
    }

    /**
     * {@code SMIX}: substitutes the 16 bytes of the logical columns 0..3 with the AES S-box,
     * applies the Super-Mix transformation and rotates row <i>r</i> left by <i>r</i> bytes.
     *
     * <p>Super-Mix is {@code W[r][c] = sum_j M[r][j] * U[j][c]  +  M[c][r] * sum_(j != r) U[r][j]}
     * with {@code U = SBOX[S[0..3]]}. Both terms are linear in the substituted bytes, so a
     * single lookup {@code SMIX_TABLE[r][x]} &ndash; the column {@code r} of M scaled by
     * {@code SBOX[x]} &ndash; can be accumulated into a column accumulator (first term) and a
     * row accumulator (second term) at the same time.</p>
     */
    private void smix() {
        final int[] index = smixIndex;
        final int[] columnAccumulator = smixColumn;
        final int[] rowAccumulator = smixRow;

        for (int c = 0; c < 4; c++) {
            index[c] = column(c);
            columnAccumulator[c] = 0;
            rowAccumulator[c] = 0;
        }
        for (int c = 0; c < 4; c++) {
            final int value = state[index[c]];
            for (int row = 0; row < 4; row++) {
                final int contribution = SMIX_TABLE[row][(value >>> (24 - 8 * row)) & 0xFF];
                columnAccumulator[c] ^= contribution;
                if (row != c) {
                    rowAccumulator[row] ^= contribution;
                }
            }
        }
        for (int c = 0; c < 4; c++) {
            state[index[c]] = 0;
        }
        for (int row = 0; row < 4; row++) {
            final int shift = 24 - 8 * row;
            for (int c = 0; c < 4; c++) {
                final int b = ((columnAccumulator[c] >>> shift) ^ (rowAccumulator[row] >>> (24 - 8 * c))) & 0xFF;
                state[index[(c - row) & 3]] |= b << shift;
            }
        }
    }

    /** The final transformation {@code G}, consisting of {@code G1[k,s,r]} and {@code G2[n,s,t]}. */
    private void finalRounds() {
        for (int i = 0; i < r * k; i++) {
            subRound();
        }
        g2();
    }

    /**
     * {@code G2[n,s,t]}: {@code t} rounds of cross mixing and {@code SMIX}, one variant per
     * number of output column groups, followed by a last cross mix.
     */
    private void g2() {
        final int p = groupStride;
        switch (groups) {
            case 1 -> {
                for (int i = 0; i < t; i++) {
                    crossMix(0, 0, 0);
                    ror(s - 1);
                    smix();
                }
                crossMix(0, 0, 0);
            }
            case 2 -> {
                for (int i = 0; i < t; i++) {
                    crossMix(p, 0, 0);
                    ror(p);
                    smix();
                    crossMix(p + 1, 0, 0);
                    ror(p - 1);
                    smix();
                }
                crossMix(p, 0, 0);
            }
            case 3 -> {
                for (int i = 0; i < t; i++) {
                    crossMix(p, 2 * p, 0);
                    ror(p);
                    smix();
                    crossMix(p + 1, 2 * p, 0);
                    ror(p);
                    smix();
                    crossMix(p + 1, 2 * p + 1, 0);
                    ror(p - 1);
                    smix();
                }
                crossMix(p, 2 * p, 0);
            }
            case 4 -> {
                for (int i = 0; i < t; i++) {
                    crossMix(p, 2 * p, 3 * p);
                    ror(p);
                    smix();
                    crossMix(p + 1, 2 * p, 3 * p);
                    ror(p);
                    smix();
                    crossMix(p + 1, 2 * p + 1, 3 * p);
                    ror(p);
                    smix();
                    crossMix(p + 1, 2 * p + 1, 3 * p + 1);
                    ror(p - 1);
                    smix();
                }
                crossMix(p, 2 * p, 3 * p);
            }
            default -> throw new IllegalStateException("unsupported number of output groups: " + groups);
        }
    }

    /**
     * Adds column 0 into column 4 and into up to three further columns; an index of 0 means
     * "no column", following the reference implementation.
     */
    private void crossMix(int a, int b, int c) {
        final int source = state[column(0)];
        state[column(4)] ^= source;
        if (a != 0) {
            state[column(a)] ^= source;
        }
        if (b != 0) {
            state[column(b)] ^= source;
        }
        if (c != 0) {
            state[column(c)] ^= source;
        }
    }

    /**
     * Selects the output columns: {@code S1..4}, then the four columns at each further group
     * offset, then as many of the columns at {@code s - stride} as are still needed.
     */
    private int[] outputWords() {
        final int[] output = new int[n];
        final int p = groupStride;
        int written = 0;
        for (int j = 0; j < 4 && j < n; j++) {
            output[written++] = state[column(j + 1)];
        }
        int group = 1;
        for (; group <= groups - 2; group++) {
            for (int j = 0; j < 4; j++) {
                output[written++] = state[column(group * p + j)];
            }
        }
        if (n > 4) {
            for (int j = 0; j + group * 4 < n; j++) {
                output[written++] = state[column(s - p + j)];
            }
        }
        if (written != n) {
            throw new IllegalStateException("produced " + written + " of " + n + " output words");
        }
        return output;
    }

    /** The output columns as big-endian bytes. */
    private byte[] outputBytes() {
        final int[] words = outputWords();
        final byte[] digest = new byte[n * 4];
        for (int i = 0; i < n; i++) {
            final int word = words[i];
            digest[4 * i] = (byte) (word >>> 24);
            digest[4 * i + 1] = (byte) (word >>> 16);
            digest[4 * i + 2] = (byte) (word >>> 8);
            digest[4 * i + 3] = (byte) word;
        }
        return digest;
    }

    // ------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------

    /**
     * Number of whole bytes of the current, incomplete input word that are already buffered.
     * Only called while the input is byte aligned, so no rounding question arises.
     */
    private int bufferedBytes() {
        return (int) ((totalBits & 31) >>> 3);
    }

    private void requireAligned() {
        if (unaligned) {
            throw new IllegalStateException(
                    "updateBits() with a bit length that is not a multiple of 8 must be the last update "
                            + "before digest() or reset()");
        }
    }

    private static int bigEndianWord(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }
}
