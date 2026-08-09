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

/*
 * Ported from the reference implementation bee2
 * (https://github.com/agievich/bee2, src/crypto/belt/belt_block.c and
 * src/crypto/belt/belt_compr.c), which is licensed under the Apache License,
 * Version 2.0:
 *
 *   Copyright (c) The Bee2 authors
 *   Licensed under the Apache License, Version 2.0
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * The Java translation has been written for Jacksum and is released under the
 * conditions of both the Apache 2.0 license and the GPLv3+.
 */

import java.util.Arrays;

/**
 * Low-level STB 34.101.31 (belt) primitives: the H substitution box, the
 * belt-block cipher (encryption direction only, as needed by belt-hash) and
 * the belt-compress compression functions used by belt-hash.
 *
 * <p>Ported byte-for-byte from the reference implementation
 * agievich/bee2 (src/crypto/belt/belt_block.c, belt_compr.c), the C library
 * maintained by the author of STB 34.101.31.
 */
final class BeltCore {

    private BeltCore() {
    }

    /** S-box H (256 bytes), STB 34.101.31 Table 1. */
    static final byte[] H = {
            (byte) 0xB1, (byte) 0x94, (byte) 0xBA, (byte) 0xC8, (byte) 0x0A, (byte) 0x08, (byte) 0xF5, (byte) 0x3B,
            (byte) 0x36, (byte) 0x6D, (byte) 0x00, (byte) 0x8E, (byte) 0x58, (byte) 0x4A, (byte) 0x5D, (byte) 0xE4,
            (byte) 0x85, (byte) 0x04, (byte) 0xFA, (byte) 0x9D, (byte) 0x1B, (byte) 0xB6, (byte) 0xC7, (byte) 0xAC,
            (byte) 0x25, (byte) 0x2E, (byte) 0x72, (byte) 0xC2, (byte) 0x02, (byte) 0xFD, (byte) 0xCE, (byte) 0x0D,
            (byte) 0x5B, (byte) 0xE3, (byte) 0xD6, (byte) 0x12, (byte) 0x17, (byte) 0xB9, (byte) 0x61, (byte) 0x81,
            (byte) 0xFE, (byte) 0x67, (byte) 0x86, (byte) 0xAD, (byte) 0x71, (byte) 0x6B, (byte) 0x89, (byte) 0x0B,
            (byte) 0x5C, (byte) 0xB0, (byte) 0xC0, (byte) 0xFF, (byte) 0x33, (byte) 0xC3, (byte) 0x56, (byte) 0xB8,
            (byte) 0x35, (byte) 0xC4, (byte) 0x05, (byte) 0xAE, (byte) 0xD8, (byte) 0xE0, (byte) 0x7F, (byte) 0x99,
            (byte) 0xE1, (byte) 0x2B, (byte) 0xDC, (byte) 0x1A, (byte) 0xE2, (byte) 0x82, (byte) 0x57, (byte) 0xEC,
            (byte) 0x70, (byte) 0x3F, (byte) 0xCC, (byte) 0xF0, (byte) 0x95, (byte) 0xEE, (byte) 0x8D, (byte) 0xF1,
            (byte) 0xC1, (byte) 0xAB, (byte) 0x76, (byte) 0x38, (byte) 0x9F, (byte) 0xE6, (byte) 0x78, (byte) 0xCA,
            (byte) 0xF7, (byte) 0xC6, (byte) 0xF8, (byte) 0x60, (byte) 0xD5, (byte) 0xBB, (byte) 0x9C, (byte) 0x4F,
            (byte) 0xF3, (byte) 0x3C, (byte) 0x65, (byte) 0x7B, (byte) 0x63, (byte) 0x7C, (byte) 0x30, (byte) 0x6A,
            (byte) 0xDD, (byte) 0x4E, (byte) 0xA7, (byte) 0x79, (byte) 0x9E, (byte) 0xB2, (byte) 0x3D, (byte) 0x31,
            (byte) 0x3E, (byte) 0x98, (byte) 0xB5, (byte) 0x6E, (byte) 0x27, (byte) 0xD3, (byte) 0xBC, (byte) 0xCF,
            (byte) 0x59, (byte) 0x1E, (byte) 0x18, (byte) 0x1F, (byte) 0x4C, (byte) 0x5A, (byte) 0xB7, (byte) 0x93,
            (byte) 0xE9, (byte) 0xDE, (byte) 0xE7, (byte) 0x2C, (byte) 0x8F, (byte) 0x0C, (byte) 0x0F, (byte) 0xA6,
            (byte) 0x2D, (byte) 0xDB, (byte) 0x49, (byte) 0xF4, (byte) 0x6F, (byte) 0x73, (byte) 0x96, (byte) 0x47,
            (byte) 0x06, (byte) 0x07, (byte) 0x53, (byte) 0x16, (byte) 0xED, (byte) 0x24, (byte) 0x7A, (byte) 0x37,
            (byte) 0x39, (byte) 0xCB, (byte) 0xA3, (byte) 0x83, (byte) 0x03, (byte) 0xA9, (byte) 0x8B, (byte) 0xF6,
            (byte) 0x92, (byte) 0xBD, (byte) 0x9B, (byte) 0x1C, (byte) 0xE5, (byte) 0xD1, (byte) 0x41, (byte) 0x01,
            (byte) 0x54, (byte) 0x45, (byte) 0xFB, (byte) 0xC9, (byte) 0x5E, (byte) 0x4D, (byte) 0x0E, (byte) 0xF2,
            (byte) 0x68, (byte) 0x20, (byte) 0x80, (byte) 0xAA, (byte) 0x22, (byte) 0x7D, (byte) 0x64, (byte) 0x2F,
            (byte) 0x26, (byte) 0x87, (byte) 0xF9, (byte) 0x34, (byte) 0x90, (byte) 0x40, (byte) 0x55, (byte) 0x11,
            (byte) 0xBE, (byte) 0x32, (byte) 0x97, (byte) 0x13, (byte) 0x43, (byte) 0xFC, (byte) 0x9A, (byte) 0x48,
            (byte) 0xA0, (byte) 0x2A, (byte) 0x88, (byte) 0x5F, (byte) 0x19, (byte) 0x4B, (byte) 0x09, (byte) 0xA1,
            (byte) 0x7E, (byte) 0xCD, (byte) 0xA4, (byte) 0xD0, (byte) 0x15, (byte) 0x44, (byte) 0xAF, (byte) 0x8C,
            (byte) 0xA5, (byte) 0x84, (byte) 0x50, (byte) 0xBF, (byte) 0x66, (byte) 0xD2, (byte) 0xE8, (byte) 0x8A,
            (byte) 0xA2, (byte) 0xD7, (byte) 0x46, (byte) 0x52, (byte) 0x42, (byte) 0xA8, (byte) 0xDF, (byte) 0xB3,
            (byte) 0x69, (byte) 0x74, (byte) 0xC5, (byte) 0x51, (byte) 0xEB, (byte) 0x23, (byte) 0x29, (byte) 0x21,
            (byte) 0xD4, (byte) 0xEF, (byte) 0xD9, (byte) 0xB4, (byte) 0x3A, (byte) 0x62, (byte) 0x28, (byte) 0x75,
            (byte) 0x91, (byte) 0x14, (byte) 0x10, (byte) 0xEA, (byte) 0x77, (byte) 0x6C, (byte) 0xDA, (byte) 0x1D,
    };

    private static final int[] H5 = new int[256];
    private static final int[] H13 = new int[256];
    private static final int[] H21 = new int[256];
    private static final int[] H29 = new int[256];

    /** Hash IV: the first 32 bytes of {@link #H}, read as 8 little-endian 32-bit words. */
    static final int[] IV;

    static {
        for (int b = 0; b < 256; b++) {
            int v = H[b] & 0xFF;
            H5[b] = Integer.rotateLeft(v, 5);
            H13[b] = Integer.rotateLeft(v, 13);
            H21[b] = Integer.rotateLeft(v, 21);
            H29[b] = Integer.rotateLeft(v, 29);
        }
        IV = bytesToWordsLE(H, 0, 8);
    }

    private static int g5(int x) {
        return H5[x & 0xFF] ^ H13[(x >>> 8) & 0xFF] ^ H21[(x >>> 16) & 0xFF] ^ H29[x >>> 24];
    }

    private static int g13(int x) {
        return H13[x & 0xFF] ^ H21[(x >>> 8) & 0xFF] ^ H29[(x >>> 16) & 0xFF] ^ H5[x >>> 24];
    }

    private static int g21(int x) {
        return H21[x & 0xFF] ^ H29[(x >>> 8) & 0xFF] ^ H5[(x >>> 16) & 0xFF] ^ H13[x >>> 24];
    }

    private static int subkey(int[] k, int i, int j) {
        return k[Math.floorMod(7 * i - 7 + j, 8)];
    }

    /** Round function R, applied to registers r[ia],r[ib],r[ic],r[id] in place. */
    private static void round(int[] r, int ia, int ib, int ic, int id, int[] k, int i) {
        int a = r[ia], b = r[ib], c = r[ic], d = r[id];
        b ^= g5(a + subkey(k, i, 0));
        c ^= g21(d + subkey(k, i, 1));
        a -= g13(b + subkey(k, i, 2));
        c += b;
        b += g21(c + subkey(k, i, 3)) ^ i;
        c -= b;
        d += g13(c + subkey(k, i, 4));
        b ^= g21(a + subkey(k, i, 5));
        c ^= g5(d + subkey(k, i, 6));
        r[ia] = a;
        r[ib] = b;
        r[ic] = c;
        r[id] = d;
    }

    private static void swap(int[] r, int i, int j) {
        int t = r[i];
        r[i] = r[j];
        r[j] = t;
    }

    /**
     * belt-block encryption (E), in place on a 16-byte block represented as
     * 4 little-endian 32-bit words, using a 256-bit key represented as 8
     * little-endian 32-bit words.
     */
    static void encryptBlock(int[] block, int[] key) {
        round(block, 0, 1, 2, 3, key, 1);
        round(block, 1, 3, 0, 2, key, 2);
        round(block, 3, 2, 1, 0, key, 3);
        round(block, 2, 0, 3, 1, key, 4);
        round(block, 0, 1, 2, 3, key, 5);
        round(block, 1, 3, 0, 2, key, 6);
        round(block, 3, 2, 1, 0, key, 7);
        round(block, 2, 0, 3, 1, key, 8);
        // final permutation abcd -> bdac
        swap(block, 0, 1);
        swap(block, 2, 3);
        swap(block, 1, 2);
    }

    private static int[] xor(int[] a, int[] b) {
        int[] out = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i] ^ b[i];
        }
        return out;
    }

    private static void xorInPlace(int[] dst, int[] src) {
        for (int i = 0; i < dst.length; i++) {
            dst[i] ^= src[i];
        }
    }

    private static int[] not(int[] a) {
        int[] out = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = ~a[i];
        }
        return out;
    }

    private static int[] concat(int[] a, int[] b) {
        int[] out = new int[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /**
     * belt-compress, belt-hash variant that also folds the compressed block
     * into the running sum {@code s} (may be {@code null} to skip that step,
     * matching plain beltCompr used only at hash finalization).
     *
     * @param s 4-word running sum accumulator, updated in place (nullable)
     * @param h 8-word hash state (h0 || h1), updated in place
     * @param x 8-word input block (X0 || X1), read-only
     */
    static void compress2(int[] s, int[] h, int[] x) {
        int[] h0 = Arrays.copyOfRange(h, 0, 4);
        int[] h1 = Arrays.copyOfRange(h, 4, 8);
        int[] x0 = Arrays.copyOfRange(x, 0, 4);
        int[] x1 = Arrays.copyOfRange(x, 4, 8);

        int[] buf0 = xor(h0, h1);
        int[] buf0Orig = buf0.clone();
        encryptBlock(buf0, x);
        xorInPlace(buf0, buf0Orig);

        if (s != null) {
            xorInPlace(s, buf0);
        }

        int[] newH0 = x0.clone();
        encryptBlock(newH0, concat(buf0, h1));
        xorInPlace(newH0, x0);

        int[] negBuf0 = not(buf0);
        int[] newH1 = x1.clone();
        encryptBlock(newH1, concat(negBuf0, h0));
        xorInPlace(newH1, x1);

        System.arraycopy(newH0, 0, h, 0, 4);
        System.arraycopy(newH1, 0, h, 4, 4);
    }

    /** belt-compress without the running-sum update, used at hash finalization. */
    static void compress(int[] h, int[] x) {
        compress2(null, h, x);
    }

    static int[] bytesToWordsLE(byte[] buf, int offset, int wordCount) {
        int[] words = new int[wordCount];
        for (int i = 0; i < wordCount; i++) {
            int p = offset + 4 * i;
            words[i] = (buf[p] & 0xFF)
                    | (buf[p + 1] & 0xFF) << 8
                    | (buf[p + 2] & 0xFF) << 16
                    | (buf[p + 3] & 0xFF) << 24;
        }
        return words;
    }

    static byte[] wordsToBytesLE(int[] words, int wordCount) {
        byte[] out = new byte[wordCount * 4];
        for (int i = 0; i < wordCount; i++) {
            int w = words[i];
            int p = 4 * i;
            out[p] = (byte) w;
            out[p + 1] = (byte) (w >>> 8);
            out[p + 2] = (byte) (w >>> 16);
            out[p + 3] = (byte) (w >>> 24);
        }
        return out;
    }
}
