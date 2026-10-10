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
 * This class implements the "Rocksoft^tm Model CRC Algorithm"
 * in the Java programming language
 *
 * For more information on the Rocksoft^tm Model CRC Algorithm, see the document
 * titled "A Painless Guide to CRC Error Detection Algorithms" by Ross
 * Williams (ross@guest.adelaide.edu.au.). This document is likely to be in
 * "ftp.adelaide.edu.au/pub/rocksoft"
 * Note: Rocksoft is a trademark of Rocksoft Pty Ltd, Adelaide, Australia.
 *
 * Ross Williams founded Rocksoft which was sold to ADIC (now Quantum) in 2006.
 * This paper can also be found on Dr. Ross Williams' homepage at
 * http://www.ross.net/crc/crcpaper.html
 */

package net.jacksum.algorithms.crcs;

import net.jacksum.zzadopt.com.github.snksoft.crc.CRC;
import net.loefflmann.sugar.util.ByteSequences;

import java.security.NoSuchAlgorithmException;

/**
 * The parameters of a CRC as defined by the Rocksoft^tm Model CRC Algorithm.
 */
public class CrcModel {

    /** The width in bits. */
    protected int width;         // the width in bits
    /** The algorithm's polynomial, specified without its top bit. */
    protected long poly;         // The algorithm's polynomial which is specified without its top bit
    /** The initial register value. */
    protected long init;         // Initial register value
    /** Whether input bytes are reflected. */
    protected boolean refIn;     // Reflect input bytes?
    /** Whether the output CRC is reflected. */
    protected boolean refOut;    // Reflect output CRC?
    /** The value that is XORed to the output CRC. */
    protected long xorOut;       // XOR this to output CRC

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
    public CrcModel(int width, long poly, long init, boolean refIn, boolean refOut, long xorOut) {
        this.width = width;
        this.poly = poly;
        this.init = init;
        this.refIn = refIn;
        this.refOut = refOut;
        this.xorOut = xorOut;
    }

    /**
     * Copy constructor.
     *
     * @param model the model to copy the parameters from
     */
    public CrcModel(CrcModel model) {
        this(model.getWidth(), model.getPoly(), model.getInit(), model.isRefIn(), model.isRefOut(), model.getXorOut());
    }

    /**
     * Constructor that takes the parameters from a CRC info object.
     *
     * @param info the object that provides the CRC parameters
     */
    public CrcModel(CrcInfo info) {
        this(info.getWidth(), bytesToLong(info.getPolyAsBytes()), info.getInitialValue(), info.isRefIn(), info.isRefOut(), info.getXorOut());
    }

    /**
     * Constructor with a String parameter.
     *
     * @param props the parameters width, poly, init, refIn, refOut and xorOut
     * separated by a comma (optionally prefixed by {@code crc:}); poly, init and
     * xorOut are hex values. Up to two further parameters are accepted.
     * @throws NoSuchAlgorithmException if there are fewer than 6 or more than 8
     * parameters, or if a parameter is invalid
     */
    public CrcModel(String props) throws NoSuchAlgorithmException {
        String[] array = props.split(",");
        if (array.length < 6) {
            throw new NoSuchAlgorithmException("Cannot create the algorithm. At least 6 parameters are expected.");
        }
        if (array.length > 8) {
            throw new NoSuchAlgorithmException("Cannot create the algorithm. No more than 8 parameters are allowed.");
        }
        if (props.startsWith("crc:")) {
            array[0] = array[0].substring(4);
        }
        try {
            width = Integer.parseInt(array[0]);
            poly = new java.math.BigInteger(array[1], 16).longValue();  //Long.parseLong(array[1], 16);
            init = new java.math.BigInteger(array[2], 16).longValue(); //Long.parseLong(array[2], 16);
            refIn = array[3].equalsIgnoreCase("true");
            refOut = array[4].equalsIgnoreCase("true");
            xorOut = new java.math.BigInteger(array[5], 16).longValue();
        } catch (NumberFormatException e) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. " + e.getMessage());
        } catch (IllegalArgumentException iae) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. "+iae.getMessage());
        }
    }

    // Transforms a byte[] to a long
    /**
     * Transforms a byte array to a long, interpreting the bytes in big-endian order.
     *
     * @param bytes the byte array, at most 8 bytes long
     * @return the long value
     * @throws IllegalArgumentException if the array is longer than 8 bytes
     */
    public static long bytesToLong(final byte[] bytes) throws IllegalArgumentException {
        if (bytes.length > Long.BYTES) throw new IllegalArgumentException("byte array length is greater than what fits into a long.");

        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result <<= Byte.SIZE;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }


    /**
     * Returns the Jacksum CRC definition string of this model, e.g.
     * {@code crc:32,04C11DB7,FFFFFFFF,true,true,FFFFFFFF}.
     *
     * @return the CRC definition string
     */
    public String getString() {
        StringBuilder sb = new StringBuilder();
        sb.append("crc:");
        int nibbles = width / 4 + ((width % 4 > 0) ? 1 : 0);
        sb.append(width);
        sb.append(",");
        sb.append(ByteSequences.hexformat(poly, nibbles).toUpperCase());
        sb.append(",");
        sb.append(ByteSequences.hexformat(init, nibbles).toUpperCase());
        sb.append(",");
        sb.append(refIn ? "true" : "false");
        sb.append(",");
        sb.append(refOut ? "true" : "false");
        sb.append(",");
        sb.append(ByteSequences.hexformat(xorOut, nibbles).toUpperCase());
        return sb.toString();
    }

    /**
     * Set the width in bits
     * @param width the width in bits
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the width in bits
     * @return the width in bits
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the algorithm's polynomial
     * @param poly the algorithm's polynomial
     */
    public void setPoly(long poly) {
        this.poly = poly;
    }

    /**
     * Get the algorithm's polynomial
     * @return the algorithm's polynomial
     */
    public long getPoly() {
        return this.poly;
    }

    /**
     * Set the initial register value
     * @param init the initial register value
     */
    public void setInit(long init) {
        this.init = init;
    }

    /**
     * Get the initial register value
     * @return the initial register value
     */
    public long getInit() {
        return init;
    }


    /**
     * Reflect input bytes?
     * @param refIn reflect input bytes?
     */
    public void setRefIn(boolean refIn) {
        this.refIn = refIn;
    }

    /**
     * Should input bytes be reflected?
     * @return should input bytes be reflected?
     */
    public boolean isRefIn() {
        return this.refIn;
    }

    /**
     * Set whether the output CRC should be reflected
     * @param refOut should the output CRC be reflected?
     */
    public void setRefOut(boolean refOut) {
        this.refOut = refOut;
    }

    /**
     * Get whether the output CRC should be reflected
     * @return should the output CRC be reflected?
     */
    public boolean isRefOut() {
        return this.refOut;
    }

    /**
     * Set the XOR parameter
     * @param xorOut the XOR parameter
     */
    public void setXorOut(long xorOut) {
        this.xorOut = xorOut;
    }

    /**
     * Get the XOR parameter
     * @return the XOR parameter
     */
    public long getXorOut() {
        return xorOut;
    }

    /**
     * Returns the parameters of this model as a {@code CRC.Parameters} object.
     *
     * @return the CRC parameters
     */
    public CRC.Parameters getParameters() {
        return new CRC.Parameters(width, poly, init, refIn, refOut, xorOut);
    }

}
