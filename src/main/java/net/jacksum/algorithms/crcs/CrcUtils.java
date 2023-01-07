/*
 * Jacksum 3.5.0 - a checksum utility in Java
 * Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

package net.jacksum.algorithms.crcs;

import net.loefflmann.sugar.util.ByteSequences;

public class CrcUtils {

    /**
     * Formats a poly dependent on width as a math expression
     * @param width the width of the poly
     * @param poly the poly stored in a Java long
     * @return a math formatted poly
     */
    public static String polyAsMathExpression(int width, long poly) {
        byte[] bytes = new byte[8];
        ByteSequences.setLongInByteArray(poly, bytes);
        return polyAsMathExpression(width, bytes);
    }

    /**
     * Formats a poly dependent on width as a math expression
     * @param width the width of the poly
     * @param poly the poly stored in a byte array
     * @return a math formatted poly
     */
    public static String polyAsMathExpression(int width, byte[] poly) {
        return polyAsMathExpression(width, ByteSequences.formatAsBits(poly));
    }

    /**
     * Formats a poly dependent on width as a math expression
     * @param width the width of the poly
     * @param bits the poly stored in a String (which may only consists of '0', and '1')
     * @return a math formatted poly
     */
    public static String polyAsMathExpression(int width, String bits) {
        StringBuilder sb = new StringBuilder();
        // this is implicitly always set
        sb.append("x^").append(width);

        int exponent;
        for (int i = 0; i < bits.length(); i++) {
            exponent = (bits.length() - 1 - i);
            if (bits.charAt(i) == '1') {
                switch (exponent) {
                    case 0:
                        sb.append(" + 1");
                        break;
                    case 1:
                        sb.append(" + x");
                        break;
                    default:
                        sb.append(" + x^").append(exponent);
                        break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Reflects a value by taking the least significant bits into account
     * Example: reflect(0x3e23L, 3) ➜ 0x3e26
     *          11111000100011 ➜ 11111000100110
     *
     * @param value the value which should be reflected
     * @param bits the number of bits to be reflected
     * @return the value with the bottom bits [0,64] reflected.
     */
    public static long reflect(long value, int bits) {
        long temp = 0L;
        for (int i = 0; i < bits; i++) {
            temp <<= 1L;
            temp |= (value & 1L);
            value >>>= 1L;
        }
        return (value << bits) | temp;
    }

    /**
     * Reverses a byte array.
     *
     * @param array the array that should be reversed.
     */
    public static void reverse(byte[] array) {
        if (array == null || array.length == 1) {
            return;
        }
        int start = 0;
        int end = array.length - 1;
        byte backup;
        while (end > start) {
            backup = array[end];
            array[end] = array[start];
            array[start] = backup;
            start++;
            end--;
        }
    }


    public static byte[] hextext2bytes(String text) throws IllegalArgumentException {
        byte[] bytes;
        // by default, a hex sequence is expected
        if ((text.length() % 2) == 1) {
            throw new IllegalArgumentException("An even number of nibbles was expected.");
        }
        try {
            bytes = new byte[text.length() / 2];
            int x = 0;
            for (int i = 0; i < text.length();) {
                String str = text.substring(i, i += 2);
                bytes[x++] = (byte) Integer.parseInt(str, 16);
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Not a hex number. " + nfe.getMessage());
        }
        return bytes;
    }

}
