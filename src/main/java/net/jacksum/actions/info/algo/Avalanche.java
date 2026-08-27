/*
 * Jacksum 4.0.1 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2024 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

package net.jacksum.actions.info.algo;

import net.jacksum.algorithms.AbstractChecksum;
import java.util.Arrays;

public class Avalanche {

    public static AvalancheInfo calc(AbstractChecksum checksum, byte[] message) {

        checksum.reset();
        checksum.update(message);
        byte[] out1 = checksum.getByteArray();

        // The byte array can be zero padded, because it stores the hash value in
        // whole bytes, while the hash value can have a bit width that is not a
        // multiple of 8 (a CRC with a width of 5 bits, for example). Padding bits
        // never flip, they must not count towards the total number of hash bits.
        int hashBits = Math.min(checksum.getOutputSizeInBits(), out1.length * 8);

        AvalancheInfo avalancheInfo = new AvalancheInfo();
        if (hashBits <= 0 || message.length == 0) {
            return avalancheInfo;
        }

        int minHammingDistance = hashBits;
        int maxHammingDistance = 0;
        long sumHammingDistances = 0;

        for (int byteIndex = 0; byteIndex < message.length; byteIndex++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {

                byte[] bytes = Arrays.copyOf(message, message.length);
                flipBit(bytes, byteIndex, bitIndex);

                checksum.reset();
                checksum.update(bytes);
                byte[] out2 = checksum.getByteArray();

                int hammingDistance = hammingDistance(out1, out2);
                sumHammingDistances += hammingDistance;
                maxHammingDistance = Math.max(maxHammingDistance, hammingDistance);
                minHammingDistance = Math.min(minHammingDistance, hammingDistance);
            }
        }

        long flips = (long) message.length * 8;
        avalancheInfo.setHammingDistanceMin(minHammingDistance * 100.0 / hashBits);
        avalancheInfo.setHammingDistanceMax(maxHammingDistance * 100.0 / hashBits);
        avalancheInfo.setHammingDistanceAvg(sumHammingDistances * 100.0 / (flips * hashBits));
        return avalancheInfo;
    }

    public static void flipBit(byte[] byteArray, int byteIndex, int bitIndex) {
        if (byteIndex < 0 || byteIndex >= byteArray.length || bitIndex < 0 || bitIndex > 7) {
            throw new IllegalArgumentException("flipBit: invalid byte or bit index");
        }
        byteArray[byteIndex] ^= (1 << 7-bitIndex);
    }

    // Calculates the sum of the bits that are set differently
    public static int hammingDistance(byte[] b1, byte[] b2) throws IllegalArgumentException {
        if (b1.length != b2.length) {
            throw new IllegalArgumentException("hammingDistance: b1.length != b2.length");
        }
        int sum = 0;
        for (int i = 0; i < b1.length; i++) {
            sum += numberOfBitsSet((byte) (b1[i] ^ b2[i]));
        }
        return sum;
    }

    // Returns the number of bits that are set in the given byte
    public static int numberOfBitsSet(byte b) {
        return Integer.bitCount(b & 0xFF);
    }
}
