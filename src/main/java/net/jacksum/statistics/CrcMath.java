/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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
package net.jacksum.statistics;

import java.math.BigInteger;
import net.loefflmann.sugar.math.GeneralMath;

/**
 * Calculates how many CRC customizations are supported by Jacksum.
 */
public class CrcMath {

    private final static int WIDTH_MIN = 8;
    private final static int WIDTH_MAX = 63;
    private final static BigInteger TWO = BigInteger.valueOf(2);

    /**
     * Creates a new CrcMath.
     */
    public CrcMath() {
    }

    /**
     * Calculates the number of supported CRC customizations for the widths
     * 8 to 63 bits, with and without the inclusion of the message length.
     *
     * @return the number in scientific notation, e.g. {@code 1.2345*10^123}
     */
    public static String calcSupportedCRCCustomizations() {
        BigInteger all =
                countPermutationsForCRCwithoutLength(WIDTH_MIN, WIDTH_MAX).
                add(countPermutationsForCRCwithLength(WIDTH_MIN, WIDTH_MAX));

        return GeneralMath.decimal2Scientific(all.toString(), 5, "*10^");
    }

    /**
     * Counts the permutations of the CRC parameters poly, init, refIn, refOut
     * and xorOut for the widths from {@code width_min} to {@code width_max} in
     * steps of 8 bits.
     *
     * @param width_min the minimum width in bits
     * @param width_max the maximum width in bits
     * @return the number of permutations
     */
    public static BigInteger countPermutationsForCRCwithoutLength(int width_min, int width_max) {
        BigInteger sum = BigInteger.ONE;

        for (int width = width_min; width <= width_max; width += 8) {
            BigInteger p = TWO.pow(width);
            sum = sum.multiply(p); // poly
            sum = sum.multiply(p); // init
            sum = sum.multiply(TWO); // refIn
            sum = sum.multiply(TWO); // refOut
            sum = sum.multiply(p); // xorOut
        }
        return sum;
    }

    /**
     * Counts the permutations of the CRC parameters including the parameters
     * for the inclusion of the message length.
     *
     * @param width_min the minimum width in bits
     * @param width_max the maximum width in bits
     * @return the number of permutations
     */
    public static BigInteger countPermutationsForCRCwithLength(int width_min, int width_max) {
        BigInteger sum = countPermutationsForCRCwithoutLength(width_min, width_max);
        sum = sum.multiply(TWO); // inclen (true and false)
        sum = sum.multiply(BigInteger.valueOf(width_max)); // max. 8 bytes for the xor-array
        return sum;
    }
}
