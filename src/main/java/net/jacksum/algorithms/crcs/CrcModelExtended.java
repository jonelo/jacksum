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

/*
 * This class implements an extention of the famous "Rocksoft^tm Model CRC Algorithm".
 * The extention was suggested by Johann N. Löfflmann as part of the Jacksum 3.0.0 release on Sept 4, 2021.
 * The extended model allows to include the (optional xor'ed) length to the CRC to describe algorithms such
 * as the POSIX cksum or the FDDI CRC (Plan 9's sum). For more information, see also the output of
 * `jacksum -h crc:`
 */

package net.jacksum.algorithms.crcs;

import net.loefflmann.sugar.util.ByteSequences;

import java.security.NoSuchAlgorithmException;

/**
 * An extension of the Rocksoft^tm Model CRC Algorithm that can optionally include the
 * (optionally XOR'ed) message length in the CRC, e.g. to describe the POSIX cksum or
 * the FDDI CRC (Plan 9's sum).
 */
public class CrcModelExtended extends CrcModel {
    private boolean includeLength = false; // include the length
    private boolean includeLengthMSOfirst = true; // include the length with the most significant octet first
    private boolean xorLength = false; // XOR the length
    private byte[] xorLengthArray; // the XOR array

    /**
     * Constructor with all parameters as defined in the
     * Rocksoft^tm Model CRC Algorithm
     * @param width width in bits
     * @param poly The algorithm's polynomial (without the highest bit)
     * @param init the initial register value
     * @param refIn Reflect input bytes?
     * @param refOut Reflect output CRC?
     * @param xorOut XOR this to output CRC
     */
    public CrcModelExtended(int width, long poly, long init, boolean refIn, boolean refOut, long xorOut) {
        super(width, poly, init, refIn, refOut, xorOut);
    }

    /**
     * Constructor with all parameters as defined in the Rocksoft^tm Model CRC
     * Algorithm, plus the inclusion of the message length.
     * @param width width in bits
     * @param poly The algorithm's polynomial (without the highest bit)
     * @param init the initial register value
     * @param refIn Reflect input bytes?
     * @param refOut Reflect output CRC?
     * @param xorOut XOR this to output CRC
     * @param includeLengthLTR true if the length is included with the most significant
     * octet first, false if with the least significant octet first
     */
    public CrcModelExtended(int width, long poly, long init, boolean refIn, boolean refOut, long xorOut,
                            boolean includeLengthLTR) {
        super(width, poly, init, refIn, refOut, xorOut);
        includeLengthInit(includeLengthLTR);
    }

    /**
     * Constructor with all parameters as defined in the Rocksoft^tm Model CRC
     * Algorithm, plus the inclusion of the message length which is XOR'ed.
     * @param width width in bits
     * @param poly The algorithm's polynomial (without the highest bit)
     * @param init the initial register value
     * @param refIn Reflect input bytes?
     * @param refOut Reflect output CRC?
     * @param xorOut XOR this to output CRC
     * @param includeLengthLTR true if the length is included with the most significant
     * octet first, false if with the least significant octet first
     * @param xorLengthArray the bytes the length is XOR'ed with
     */
    public CrcModelExtended(int width, long poly, long init, boolean refIn, boolean refOut, long xorOut,
                            boolean includeLengthLTR,  byte[] xorLengthArray) {
        super(width, poly, init, refIn, refOut, xorOut);
        includeLengthInit(includeLengthLTR);
        xorLengthArrayInit(xorLengthArray);
    }

    /**
     * Constructor with a String parameter.
     *
     * @param props the parameters width, poly, init, refIn, refOut and xorOut
     * separated by a comma (optionally prefixed by {@code crc:}), optionally followed
     * by includeLengthMSOfirst (true or false) and by the hex value the length is
     * XOR'ed with
     * @throws NoSuchAlgorithmException if the number of parameters is wrong, or if a
     * parameter is invalid
     */
    public CrcModelExtended(String props) throws NoSuchAlgorithmException {
        super(props);
        String[] array = props.split(",");
        // 7th and 8th parameter (array index 6 and 7) are optional
        if (array.length >= 7) {
            includeLength = true;
            includeLengthMSOfirst = array[6].equalsIgnoreCase("true");
            if (array.length >= 8) {
                xorLength = true;
                xorLengthArray = CrcUtils.hextext2bytes(array[7]);
            }
        }
    }

    /**
     * Creates a copy of the given model, including the settings for the length
     * (whether it is included, its byte order, and whether and with what it is XOR'ed).
     *
     * @param model the model that is copied
     */
    public CrcModelExtended(CrcModelExtended model) {
        super(model.getWidth(), model.getPoly(), model.getInit(), model.isRefIn(), model.isRefOut(), model.getXorOut());
        includeLength = model.isIncludeLength();
        includeLengthMSOfirst = model.isIncludeLengthMSOfirst();
        xorLength = model.isXorLength();
        byte[] array = model.getXorLengthArray();
        xorLengthArray = array == null ? null : array.clone();
    }


    public String getString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getString());
        if (includeLength) {
            sb.append(",");
            sb.append(includeLengthMSOfirst ? "true" : "false");
            if (xorLength) {
                sb.append(",");
                sb.append(ByteSequences.format(xorLengthArray, true));
            }
        }
        return sb.toString();
    }


    private void includeLengthInit(boolean includeLengthMSO) {
        this.includeLength = true;
        this.includeLengthMSOfirst = includeLengthMSO;
    }

    private void xorLengthArrayInit(byte[] xorLengthArray) {
        this.xorLength = true;
        this.xorLengthArray = xorLengthArray;
    }

    /**
     * Tells whether the message length is included in the CRC.
     *
     * @return true if the length is included
     */
    public boolean isIncludeLength() {
        return includeLength;
    }

    /**
     * Tells whether the included message length is XOR'ed.
     *
     * @return true if the length is XOR'ed
     */
    public boolean isXorLength() {
        return xorLength;
    }

    /**
     * Tells whether the length is included with the most significant octet first.
     *
     * @return true if the most significant octet comes first
     */
    public boolean isIncludeLengthMSOfirst() {
        return includeLengthMSOfirst;
    }

    /**
     * Returns the bytes the included message length is XOR'ed with.
     *
     * @return the XOR length array, or null if the length is not XOR'ed
     */
    public byte[] getXorLengthArray() {
        if (xorLength) {
            return xorLengthArray;
        } else {
            return null;
        }
    }

}
