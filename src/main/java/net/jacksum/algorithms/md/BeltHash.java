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

package net.jacksum.algorithms.md;

/*
 * belt-hash as specified by STB 34.101.31 (Republic of Belarus).
 *
 * The low level primitives in BeltCore have been ported from the reference
 * implementation bee2 (https://github.com/agievich/bee2), which is licensed
 * under the Apache License, Version 2.0:
 *
 *   Copyright (c) The Bee2 authors
 *   Licensed under the Apache License, Version 2.0
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * The Java translation has been written for Jacksum and is released under the
 * conditions of both the Apache 2.0 license and the GPLv3+.
 */

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * {@link MessageDigest} implementation of belt-hash, STB 34.101.31 (Republic
 * of Belarus). Produces a 256-bit (32-byte) digest.
 *
 * <p>Usage: {@code MessageDigest md = new BeltHash();} then use like any
 * other {@code MessageDigest} (e.g. with {@link java.security.DigestInputStream}).
 */
public final class BeltHash extends MessageDigest {

    private static final int BLOCK_BYTES = 32;

    private final byte[] buffer = new byte[BLOCK_BYTES];
    private int filled;
    private long bitLength;
    private final int[] s = new int[4];
    private final int[] h = new int[8];

    public BeltHash() {
        super("BELT");
        resetState();
    }

    private void resetState() {
        Arrays.fill(buffer, (byte) 0);
        filled = 0;
        bitLength = 0L;
        Arrays.fill(s, 0);
        System.arraycopy(BeltCore.IV, 0, h, 0, 8);
    }

    @Override
    protected int engineGetDigestLength() {
        return BLOCK_BYTES;
    }

    @Override
    protected void engineUpdate(byte input) {
        bitLength += 8L;
        buffer[filled++] = input;
        if (filled == BLOCK_BYTES) {
            BeltCore.compress2(s, h, BeltCore.bytesToWordsLE(buffer, 0, 8));
            filled = 0;
        }
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        bitLength += (long) len * 8L;
        int inOff = offset;
        int remaining = len;

        if (filled > 0) {
            int take = Math.min(BLOCK_BYTES - filled, remaining);
            System.arraycopy(input, inOff, buffer, filled, take);
            filled += take;
            inOff += take;
            remaining -= take;
            if (filled < BLOCK_BYTES) {
                return;
            }
            BeltCore.compress2(s, h, BeltCore.bytesToWordsLE(buffer, 0, 8));
            filled = 0;
        }

        while (remaining >= BLOCK_BYTES) {
            BeltCore.compress2(s, h, BeltCore.bytesToWordsLE(input, inOff, 8));
            inOff += BLOCK_BYTES;
            remaining -= BLOCK_BYTES;
        }

        if (remaining > 0) {
            System.arraycopy(input, inOff, buffer, 0, remaining);
            filled = remaining;
        }
    }

    @Override
    protected byte[] engineDigest() {
        if (filled > 0) {
            Arrays.fill(buffer, filled, BLOCK_BYTES, (byte) 0);
            BeltCore.compress2(s, h, BeltCore.bytesToWordsLE(buffer, 0, 8));
        }

        int[] lenAndSum = new int[8];
        lenAndSum[0] = (int) bitLength;
        lenAndSum[1] = (int) (bitLength >>> 32);
        // lenAndSum[2], lenAndSum[3] stay 0: the full belt-hash length counter
        // is 128 bits, but a 64-bit bit-length (up to ~2 exbibytes) is ample
        // for a file-hashing tool.
        System.arraycopy(s, 0, lenAndSum, 4, 4);

        BeltCore.compress(h, lenAndSum);
        byte[] digest = BeltCore.wordsToBytesLE(h, 8);

        resetState();
        return digest;
    }

    @Override
    protected void engineReset() {
        resetState();
    }
}
