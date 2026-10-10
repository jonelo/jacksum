/*
 * Jacksum 4.0.2 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

/**
 * Provides the parameters of a CRC (as in the Rocksoft model).
 */
public interface CrcInfo {
    /**
     * Gets the generator polynomial as a big-endian byte array.
     *
     * @return the polynomial as bytes
     */
    public byte[] getPolyAsBytes();

    /**
     * Gets the width of the CRC.
     *
     * @return the width in bits
     */
    public int getWidth();

    /**
     * Gets the initial value of the CRC register.
     *
     * @return the initial value
     */
    public long getInitialValue();

    /**
     * Tells whether the input bytes are reflected.
     *
     * @return true if the input bytes are reflected
     */
    public boolean isRefIn();

    /**
     * Tells whether the final CRC value is reflected before the final XOR.
     *
     * @return true if the output is reflected
     */
    public boolean isRefOut();

    /**
     * Gets the value that is XORed to the final CRC value.
     *
     * @return the final XOR value
     */
    public long getXorOut();
}
