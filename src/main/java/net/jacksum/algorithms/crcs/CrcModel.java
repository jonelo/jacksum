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

public class CrcModel {

    protected int width;         // the width in bits
    protected long poly;         // The algorithm's polynomial which is specified without its top bit
    protected long init;         // Initial register value
    protected boolean refIn;     // Reflect input bytes?
    protected boolean refOut;    // Reflect output CRC?
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

    public CrcModel(CrcModel model) {
        this(model.getWidth(), model.getPoly(), model.getInit(), model.isRefIn(), model.isRefOut(), model.getXorOut());
    }

    public CrcModel(String props) throws NoSuchAlgorithmException {
        String[] array = props.split(",");
        if (array.length < 6) {
            throw new NoSuchAlgorithmException("Can't create the algorithm, at least 6 parameters are expected.");
        }
        if (array.length > 8) {
            throw new NoSuchAlgorithmException("Can't create the algorithm, no more than 8 parameters are allowed.");
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
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. " + e);
        } catch (IllegalArgumentException iae) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. "+iae.getMessage());
        }
    }

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

    public CRC.Parameters getParameters() {
        return new CRC.Parameters(width, poly, init, refIn, refOut, xorOut);
    }

}
