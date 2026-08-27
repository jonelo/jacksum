/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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

package net.jacksum.algorithms.checksums;

import java.security.NoSuchAlgorithmException;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * Generic n-bit FNV-0 (and, via subclasses, FNV-1/FNV-1a) implementation.
 *
 * <p>The state is kept as a fixed-size little-endian array of 64-bit limbs
 * ({@code value}) rather than a {@link java.math.BigInteger}, so the per-byte
 * hashing loop does primitive arithmetic without allocating objects. Every
 * supported FNV prime is sparse, {@code prime = 2^k + 2^8 + b} (with a small
 * {@code b < 256}), so {@code value * prime mod 2^width} is computed as three
 * shifted/scaled additions ({@code (value << k) + (value << 8) + value*b}),
 * mirroring the trick already used by {@link Fnv0_64}. The modulo {@code 2^width}
 * is just truncation to {@code nlimbs} limbs plus masking the most significant
 * limb ({@code topMask}).</p>
 */
public class Fnv0_n extends AbstractChecksum {

    protected long[] value;    // current state, little-endian 64-bit limbs
    protected long[] scratch;  // reused work buffer to avoid per-byte allocation
    protected long[] initLimbs; // starting value (all zero for FNV-0)
    protected int nlimbs;      // number of 64-bit limbs = ceil(width/64)
    protected long topMask;    // mask applied to the most significant limb
    protected int primeK;      // prime = 2^primeK + 2^8 + primeB
    protected long primeB;

    int targetsize = 0; // in bytes

    public Fnv0_n(int width) throws NoSuchAlgorithmException {
        super();
        init(width);
    }

    public Fnv0_n(String width) throws NoSuchAlgorithmException {
        super();
        try {
            bitWidth = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            throw new NoSuchAlgorithmException(String.format("Unknown algorithm: not a number. %s", e));
        }
        init(bitWidth);
    }

    private void init(int width) throws NoSuchAlgorithmException {
        // check validity of the width
        if (width < 32 || width > 1024) {
            throw new NoSuchAlgorithmException(String.format("Unknown algorithm: width %s is not supported.", width));
        }
        this.bitWidth = width;

        // initialize formatPreferences
        formatPreferences.setSeparator(" ");
        if (width <= 32) {
            formatPreferences.setHashEncoding(Encoding.DEC);
            formatPreferences.setFilesizeWanted(true);
        } else {
            formatPreferences.setHashEncoding(Encoding.HEX);
            formatPreferences.setFilesizeWanted(false);
        }

        // initialize members dependent on the width.
        // Every FNV prime has the form 2^primeK + 2^8 + primeB.
        switch (width) {
            case 32:
                primeK = 24; primeB = 0x93; // prime = 16777619
                break;
            case 64:
                primeK = 40; primeB = 0xb3; // prime = 1099511628211
                break;
            case 128:
                primeK = 88; primeB = 0x3b; // prime = 309485009821345068724781371
                break;
            case 256:
                primeK = 168; primeB = 0x63;
                break;
            case 512:
                primeK = 344; primeB = 0x57;
                break;
            case 1024:
                primeK = 680; primeB = 0x8d;
                break;
            default:
                throw new NoSuchAlgorithmException(String.format("Unknown algorithm: width %s is not supported.", width));
        }

        nlimbs = (width + 63) >>> 6;
        int topBits = width & 63;
        topMask = (topBits == 0) ? -1L : (1L << topBits) - 1L;
        targetsize = width / 8;

        value = new long[nlimbs];
        scratch = new long[nlimbs];
        initLimbs = new long[nlimbs]; // FNV-0 starts at zero
    }

    /**
     * {@code acc += (src << s)} modulo {@code 2^width}; bits shifted beyond the
     * top limb are discarded (that is the modulo).
     */
    protected void addShiftedInto(long[] acc, long[] src, int s) {
        int ws = s >>> 6;
        int bs = s & 63;
        long carry = 0;
        for (int d = ws; d < nlimbs; d++) {
            int i = d - ws;
            long piece = src[i] << bs;
            if (bs != 0 && i - 1 >= 0) {
                piece |= src[i - 1] >>> (64 - bs);
            }
            long s1 = acc[d] + piece;
            long c1 = Long.compareUnsigned(s1, piece) < 0 ? 1 : 0;
            long s2 = s1 + carry;
            long c2 = Long.compareUnsigned(s2, s1) < 0 ? 1 : 0;
            acc[d] = s2;
            carry = c1 + c2;
        }
    }

    /**
     * {@code acc += src * f} modulo {@code 2^width}, with {@code f < 256}.
     */
    protected void addScaledInto(long[] acc, long[] src, long f) {
        long carry = 0;
        for (int d = 0; d < nlimbs; d++) {
            long lo = src[d] * f;
            long hi = Math.unsignedMultiplyHigh(src[d], f); // < 256
            long s1 = acc[d] + lo;
            long c1 = Long.compareUnsigned(s1, lo) < 0 ? 1 : 0;
            long s2 = s1 + carry;
            long c2 = Long.compareUnsigned(s2, s1) < 0 ? 1 : 0;
            acc[d] = s2;
            carry = hi + c1 + c2;
        }
    }

    /**
     * {@code value = (value * prime) mod 2^width}, using the sparse prime
     * decomposition {@code prime = 2^primeK + 2^8 + primeB}. The result is
     * produced in {@code scratch} and swapped into {@code value}.
     */
    protected void multiplyByPrime() {
        java.util.Arrays.fill(scratch, 0L);
        addShiftedInto(scratch, value, primeK); // value << primeK
        addShiftedInto(scratch, value, 8);      // value << 8
        addScaledInto(scratch, value, primeB);  // value * primeB
        scratch[nlimbs - 1] &= topMask;
        long[] t = value;
        value = scratch;
        scratch = t;
    }

    @Override
    public void reset() {
        System.arraycopy(initLimbs, 0, value, 0, nlimbs);
        length = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            multiplyByPrime();
            value[0] ^= (bytes[i] & 0xFF);
        }
        this.length += length;
    }


    @Override
    public byte[] getByteArray() {
        byte[] target = new byte[targetsize];
        for (int i = 0; i < targetsize; i++) { // i = byte index counting from the LSB
            long limb = value[i >>> 3];
            target[targetsize - 1 - i] = (byte) (limb >>> ((i & 7) << 3));
        }
        return target;
    }
}
