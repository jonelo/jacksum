/*
 * Jacksum 3.8.0 - a checksum utility in Java
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
//import net.loefflmann.sugar.util.ByteSequences;

public class Avalanche {
    public static AvalancheInfo calc(AbstractChecksum checksum, byte[] message) {

        double min=100.0, max=0.0;
        double avg = 0.0;
        for (int byteIndex=0; byteIndex < message.length; byteIndex++) {
            for (int bitIndex=0; bitIndex < 8; bitIndex++) {

                byte[] bytes = Arrays.copyOf(message, message.length);
//                System.out.println("bin(string original)= " + ByteSequences.formatAsBits(bytes));
                checksum.reset();
                checksum.update(bytes);
                byte[] out1 = checksum.getByteArray();
//                System.out.println("algo(string original)=" + ByteSequences.format(out1, false));

                // the first bit in big endian representation (from the right hand side) ???


                flipBit(bytes,  byteIndex,  bitIndex);


//                System.out.println("bin(string flipped)=  " + ByteSequences.formatAsBits(bytes));

                checksum.reset();
                checksum.update(bytes);
                byte[] out2 = checksum.getByteArray();
//                System.out.println("algo(string flipped)= " + ByteSequences.format(out2, false));

                int hammingDistance = hammingDistance(out1, out2);
//                System.out.println("hamming distance=" + hammingDistance);
//                System.out.println("hash len in bits=" + (out1.length * 8));
                double result = hammingDistance * 100.0 / (out1.length*8);
                avg += result;
                max = Math.max(max, result);
                min = Math.min(min, result);
            }

        }
        avg /= (message.length*8);
        AvalancheInfo avalancheInfo = new AvalancheInfo();
        avalancheInfo.setHammingDistanceMin(min);
        avalancheInfo.setHammingDistanceMax(max);
        avalancheInfo.setHammingDistanceAvg(avg);
        return avalancheInfo;
    }

    public static void flipBit(byte[] byteArray, int byteIndex, int bitIndex) {
        if (byteIndex < 0 || byteIndex >= byteArray.length || bitIndex < 0 || bitIndex > 7) {
            throw new IllegalArgumentException("Ungültiger Byte- oder Bit-Index");
        }
        byteArray[byteIndex] ^= (1 << 7-bitIndex);
    }


/*
    public static void main(String[] args) {
        byte[] b1 = {5, 7}; // 0b00000101, 0b00000111
        byte[] b2 = {6, 8}; // 0b00000110, 0b00001000
        System.out.println(hammingDistance(b1, b2)); // Output: 6
    }
*/
    // Berechnet die Summe der unterschiedlich gesetzten Bits
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

    // Eine Iteration für jedes Bit, insgesamt 8. Rechtsverschiebung und AND 1, um das i-te Bit zu erhalten
    public static int numberOfBitsSet(byte b) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            count += (b >>> i) & 1;
        }
        return count;
    }
}
