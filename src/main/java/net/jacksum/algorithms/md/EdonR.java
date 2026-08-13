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

import java.security.DigestException;
import java.security.MessageDigest;

/**
 * {@link MessageDigest} implementation of Edon-R, for digest sizes 224, 256, 384 and 512 bits.
 *
 * <p>Java port of the Edon-R reference implementation ({@code Edon-R_ref.c}) as submitted by
 * Danilo Gligoroski et al. to the NIST SHA-3 competition in 2008.
 *
 * <p>Unlike the C reference (which exposes a bit-length {@code Update}/{@code Final} API per the
 * NIST SHA-3 submission format and restricts consecutive {@code Update} calls to one block worth
 * of buffered data), this class works purely in whole bytes, as required by
 * {@link MessageDigest}, and buffers across an arbitrary number of {@code update} calls of
 * arbitrary size.
 *
 * <p>Usage: {@code MessageDigest md = new EdonR(512);} then use like any other
 * {@code MessageDigest}.
 */
public final class EdonR extends MessageDigest implements Cloneable {

    private static final int[] I224P2 = {
        0x00010203, 0x04050607, 0x08090a0b, 0x0c0d0e0f,
        0x10111213, 0x14151617, 0x18191a1b, 0x1c1d1e1f,
        0x20212223, 0x24252627, 0x28292a2b, 0x2c2d2e2f,
        0x30313233, 0x24353637, 0x38393a3b, 0x3c3d3e3f,
    };
    private static final int[] I256P2 = {
        0x40414243, 0x44454647, 0x48494a4b, 0x4c4d4e4f,
        0x50515253, 0x54555657, 0x58595a5b, 0x5c5d5e5f,
        0x60616263, 0x64656667, 0x68696a6b, 0x6c6d6e6f,
        0x70717273, 0x74757677, 0x78797a7b, 0x7c7d7e7f,
    };
    private static final long[] I384P2 = {
        0x0001020304050607L, 0x08090a0b0c0d0e0fL,
        0x1011121314151617L, 0x18191a1b1c1d1e1fL,
        0x2021222324252627L, 0x28292a2b2c2d2e2fL,
        0x3031323324353637L, 0x38393a3b3c3d3e3fL,
        0x4041424344454647L, 0x48494a4b4c4d4e4fL,
        0x5051525354555657L, 0x58595a5b5c5d5e5fL,
        0x6061626364656667L, 0x68696a6b6c6d6e6fL,
        0x7071727374757677L, 0x78797a7b7c7d7e7fL,
    };
    private static final long[] I512P2 = {
        0x8081828384858687L, 0x88898a8b8c8d8e8fL,
        0x9091929394959697L, 0x98999a9b9c9d9e9fL,
        0xa0a1a2a3a4a5a6a7L, 0xa8a9aaabacadaeafL,
        0xb0b1b2b3b4b5b6b7L, 0xb8b9babbbcbdbebfL,
        0xc0c1c2c3c4c5c6c7L, 0xc8c9cacbcccdcecfL,
        0xd0d1d2d3d4d5d6d7L, 0xd8d9dadbdcdddedfL,
        0xe0e1e2e3e4e5e6e7L, 0xe8e9eaebecedeeefL,
        0xf0f1f2f3f4f5f6f7L, 0xf8f9fafbfcfdfeffL,
    };

    private final int digestBits;
    private final int digestBytes;
    private final int blockSize;
    private final boolean use64;

    private int[] pipe32;
    private long[] pipe64;
    private byte[] buffer;
    private int bufferLen;
    private long bytesProcessed;

    public EdonR(int digestBits) {
        super("Edon-R-" + digestBits);
        switch (digestBits) {
            case 224:
                this.digestBits = 224;
                this.digestBytes = 28;
                this.blockSize = 64;
                this.use64 = false;
                break;
            case 256:
                this.digestBits = 256;
                this.digestBytes = 32;
                this.blockSize = 64;
                this.use64 = false;
                break;
            case 384:
                this.digestBits = 384;
                this.digestBytes = 48;
                this.blockSize = 128;
                this.use64 = true;
                break;
            case 512:
                this.digestBits = 512;
                this.digestBytes = 64;
                this.blockSize = 128;
                this.use64 = true;
                break;
            default:
                throw new IllegalArgumentException("Unsupported Edon-R digest size: " + digestBits
                    + " (must be 224, 256, 384 or 512)");
        }
        this.buffer = new byte[blockSize];
        resetState();
    }

    private void resetState() {
        if (use64) {
            pipe64 = new long[32];
            System.arraycopy(digestBits == 384 ? I384P2 : I512P2, 0, pipe64, 0, 16);
            pipe32 = null;
        } else {
            pipe32 = new int[32];
            System.arraycopy(digestBits == 224 ? I224P2 : I256P2, 0, pipe32, 0, 16);
            pipe64 = null;
        }
        bufferLen = 0;
        bytesProcessed = 0;
    }

    @Override
    protected int engineGetDigestLength() {
        return digestBytes;
    }

    @Override
    protected void engineReset() {
        resetState();
    }

    @Override
    protected void engineUpdate(byte input) {
        engineUpdate(new byte[] { input }, 0, 1);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        int inPos = offset;
        int remaining = len;

        if (bufferLen > 0) {
            int need = blockSize - bufferLen;
            int take = Math.min(need, remaining);
            System.arraycopy(input, inPos, buffer, bufferLen, take);
            bufferLen += take;
            inPos += take;
            remaining -= take;
            if (bufferLen < blockSize) {
                return;
            }
            compressBlock(buffer, 0);
            bytesProcessed += blockSize;
            bufferLen = 0;
        }

        while (remaining >= blockSize) {
            compressBlock(input, inPos);
            bytesProcessed += blockSize;
            inPos += blockSize;
            remaining -= blockSize;
        }

        if (remaining > 0) {
            System.arraycopy(input, inPos, buffer, 0, remaining);
            bufferLen = remaining;
        }
    }

    @Override
    protected byte[] engineDigest() {
        int padBufSize = (bufferLen < blockSize - 8) ? blockSize : blockSize * 2;
        byte[] finalBuf = new byte[padBufSize];
        System.arraycopy(buffer, 0, finalBuf, 0, bufferLen);
        finalBuf[bufferLen] = (byte) 0x80;

        long totalBits = (bytesProcessed + bufferLen) * 8;
        for (int i = 0; i < 8; i++) {
            finalBuf[padBufSize - 8 + i] = (byte) (totalBits >>> (8 * i));
        }

        for (int off = 0; off < padBufSize; off += blockSize) {
            compressBlock(finalBuf, off);
        }

        byte[] digest;
        if (use64) {
            int startWord = (digestBits == 384) ? 10 : 8;
            digest = wordsToBytes64(pipe64, startWord, digestBytes);
        } else {
            int startWord = (digestBits == 224) ? 9 : 8;
            digest = wordsToBytes32(pipe32, startWord, digestBytes);
        }

        resetState();
        return digest;
    }

    @Override
    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        byte[] digest = engineDigest();
        if (len < digest.length) {
            throw new DigestException("partial digests not returned");
        }
        if (buf.length - offset < digest.length) {
            throw new DigestException("insufficient space in the output buffer to store the digest");
        }
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        EdonR copy = (EdonR) super.clone();
        copy.pipe32 = (pipe32 != null) ? pipe32.clone() : null;
        copy.pipe64 = (pipe64 != null) ? pipe64.clone() : null;
        copy.buffer = buffer.clone();
        return copy;
    }

    private void compressBlock(byte[] src, int off) {
        if (use64) {
            long[] m = new long[16];
            for (int i = 0; i < 16; i++) {
                m[i] = bytesToWord64(src, off + i * 8);
            }
            compressBlock512(m);
        } else {
            int[] m = new int[16];
            for (int i = 0; i < 16; i++) {
                m[i] = bytesToWord32(src, off + i * 4);
            }
            compressBlock256(m);
        }
    }

    private void compressBlock256(int[] m) {
        int[] p = pipe32;
        int[] t = new int[8];

        q256(m[15], m[14], m[13], m[12], m[11], m[10], m[9], m[8],
             m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], t);
        setWords(p, 16, t);

        q256(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15], t);
        setWords(p, 24, t);

        q256(p[8], p[9], p[10], p[11], p[12], p[13], p[14], p[15],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 16, t);

        q256(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31], t);
        setWords(p, 24, t);

        q256(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], t);
        setWords(p, 16, t);

        q256(p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 24, t);

        q256(m[7], m[6], m[5], m[4], m[3], m[2], m[1], m[0],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 0, t);

        q256(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7],
             p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31], t);
        setWords(p, 8, t);
    }

    private void compressBlock512(long[] m) {
        long[] p = pipe64;
        long[] t = new long[8];

        q512(m[15], m[14], m[13], m[12], m[11], m[10], m[9], m[8],
             m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], t);
        setWords(p, 16, t);

        q512(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15], t);
        setWords(p, 24, t);

        q512(p[8], p[9], p[10], p[11], p[12], p[13], p[14], p[15],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 16, t);

        q512(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31], t);
        setWords(p, 24, t);

        q512(p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23],
             p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], t);
        setWords(p, 16, t);

        q512(p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 24, t);

        q512(m[7], m[6], m[5], m[4], m[3], m[2], m[1], m[0],
             p[16], p[17], p[18], p[19], p[20], p[21], p[22], p[23], t);
        setWords(p, 0, t);

        q512(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7],
             p[24], p[25], p[26], p[27], p[28], p[29], p[30], p[31], t);
        setWords(p, 8, t);
    }

    private static void setWords(int[] p, int base, int[] t) {
        System.arraycopy(t, 0, p, base, 8);
    }

    private static void setWords(long[] p, int base, long[] t) {
        System.arraycopy(t, 0, p, base, 8);
    }

    /** First Latin square, then second orthogonal Latin square (32-bit quasigroup transform). */
    private static void q256(int x0, int x1, int x2, int x3, int x4, int x5, int x6, int x7,
                              int y0, int y1, int y2, int y3, int y4, int y5, int y6, int y7,
                              int[] z) {
        int t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15;

        t0 = 0xaaaaaaaa + x0 + x1 + x2 + x4 + x7;
        t1 = x0 + x1 + x3 + x4 + x7;
        t2 = x0 + x1 + x4 + x6 + x7;
        t3 = x2 + x3 + x5 + x6 + x7;
        t4 = x1 + x2 + x3 + x5 + x6;
        t5 = x0 + x2 + x3 + x4 + x5;
        t6 = x0 + x1 + x5 + x6 + x7;
        t7 = x2 + x3 + x4 + x5 + x6;
        t1 = rotl32(t1, 4);
        t2 = rotl32(t2, 8);
        t3 = rotl32(t3, 13);
        t4 = rotl32(t4, 17);
        t5 = rotl32(t5, 22);
        t6 = rotl32(t6, 24);
        t7 = rotl32(t7, 29);

        t8 = t3 ^ t5 ^ t6;
        t9 = t2 ^ t5 ^ t6;
        t10 = t2 ^ t3 ^ t5;
        t11 = t0 ^ t1 ^ t4;
        t12 = t0 ^ t4 ^ t7;
        t13 = t1 ^ t6 ^ t7;
        t14 = t2 ^ t3 ^ t4;
        t15 = t0 ^ t1 ^ t7;

        t0 = 0x55555555 + y0 + y1 + y2 + y5 + y7;
        t1 = y0 + y1 + y3 + y4 + y6;
        t2 = y0 + y1 + y2 + y3 + y5;
        t3 = y2 + y3 + y4 + y6 + y7;
        t4 = y0 + y1 + y3 + y4 + y5;
        t5 = y2 + y4 + y5 + y6 + y7;
        t6 = y1 + y2 + y5 + y6 + y7;
        t7 = y0 + y3 + y4 + y6 + y7;
        t1 = rotl32(t1, 5);
        t2 = rotl32(t2, 9);
        t3 = rotl32(t3, 11);
        t4 = rotl32(t4, 15);
        t5 = rotl32(t5, 20);
        t6 = rotl32(t6, 25);
        t7 = rotl32(t7, 27);

        z[5] = t8 + (t3 ^ t4 ^ t6);
        z[6] = t9 + (t2 ^ t5 ^ t7);
        z[7] = t10 + (t4 ^ t6 ^ t7);
        z[0] = t11 + (t0 ^ t1 ^ t5);
        z[1] = t12 + (t2 ^ t6 ^ t7);
        z[2] = t13 + (t0 ^ t1 ^ t3);
        z[3] = t14 + (t0 ^ t3 ^ t4);
        z[4] = t15 + (t1 ^ t2 ^ t5);
    }

    /** First Latin square, then second orthogonal Latin square (64-bit quasigroup transform). */
    private static void q512(long x0, long x1, long x2, long x3, long x4, long x5, long x6, long x7,
                              long y0, long y1, long y2, long y3, long y4, long y5, long y6, long y7,
                              long[] z) {
        long t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15;

        t0 = 0xaaaaaaaaaaaaaaaaL + x0 + x1 + x2 + x4 + x7;
        t1 = x0 + x1 + x3 + x4 + x7;
        t2 = x0 + x1 + x4 + x6 + x7;
        t3 = x2 + x3 + x5 + x6 + x7;
        t4 = x1 + x2 + x3 + x5 + x6;
        t5 = x0 + x2 + x3 + x4 + x5;
        t6 = x0 + x1 + x5 + x6 + x7;
        t7 = x2 + x3 + x4 + x5 + x6;
        t1 = rotl64(t1, 5);
        t2 = rotl64(t2, 15);
        t3 = rotl64(t3, 22);
        t4 = rotl64(t4, 31);
        t5 = rotl64(t5, 40);
        t6 = rotl64(t6, 50);
        t7 = rotl64(t7, 59);

        t8 = t3 ^ t5 ^ t6;
        t9 = t2 ^ t5 ^ t6;
        t10 = t2 ^ t3 ^ t5;
        t11 = t0 ^ t1 ^ t4;
        t12 = t0 ^ t4 ^ t7;
        t13 = t1 ^ t6 ^ t7;
        t14 = t2 ^ t3 ^ t4;
        t15 = t0 ^ t1 ^ t7;

        t0 = 0x5555555555555555L + y0 + y1 + y2 + y5 + y7;
        t1 = y0 + y1 + y3 + y4 + y6;
        t2 = y0 + y1 + y2 + y3 + y5;
        t3 = y2 + y3 + y4 + y6 + y7;
        t4 = y0 + y1 + y3 + y4 + y5;
        t5 = y2 + y4 + y5 + y6 + y7;
        t6 = y1 + y2 + y5 + y6 + y7;
        t7 = y0 + y3 + y4 + y6 + y7;
        t1 = rotl64(t1, 10);
        t2 = rotl64(t2, 19);
        t3 = rotl64(t3, 29);
        t4 = rotl64(t4, 36);
        t5 = rotl64(t5, 44);
        t6 = rotl64(t6, 48);
        t7 = rotl64(t7, 55);

        z[5] = t8 + (t3 ^ t4 ^ t6);
        z[6] = t9 + (t2 ^ t5 ^ t7);
        z[7] = t10 + (t4 ^ t6 ^ t7);
        z[0] = t11 + (t0 ^ t1 ^ t5);
        z[1] = t12 + (t2 ^ t6 ^ t7);
        z[2] = t13 + (t0 ^ t1 ^ t3);
        z[3] = t14 + (t0 ^ t3 ^ t4);
        z[4] = t15 + (t1 ^ t2 ^ t5);
    }

    private static int rotl32(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    private static long rotl64(long x, int n) {
        return (x << n) | (x >>> (64 - n));
    }

    private static int bytesToWord32(byte[] src, int off) {
        return (src[off] & 0xff)
            | ((src[off + 1] & 0xff) << 8)
            | ((src[off + 2] & 0xff) << 16)
            | ((src[off + 3] & 0xff) << 24);
    }

    private static long bytesToWord64(byte[] src, int off) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= (src[off + i] & 0xffL) << (8 * i);
        }
        return v;
    }

    private static byte[] wordsToBytes32(int[] words, int startWord, int byteCount) {
        byte[] out = new byte[byteCount];
        int pos = 0;
        int word = startWord;
        while (pos < byteCount) {
            int v = words[word++];
            for (int i = 0; i < 4 && pos < byteCount; i++, pos++) {
                out[pos] = (byte) (v >>> (8 * i));
            }
        }
        return out;
    }

    private static byte[] wordsToBytes64(long[] words, int startWord, int byteCount) {
        byte[] out = new byte[byteCount];
        int pos = 0;
        int word = startWord;
        while (pos < byteCount) {
            long v = words[word++];
            for (int i = 0; i < 8 && pos < byteCount; i++, pos++) {
                out[pos] = (byte) (v >>> (8 * i));
            }
        }
        return out;
    }
}
