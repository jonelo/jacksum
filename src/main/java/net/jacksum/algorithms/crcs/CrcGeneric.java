/*


  Jacksum 3.2.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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


/*
 * This class implements the "Rocksoft^tm Model CRC Algorithm"
 * in the Java programming language
 *
 * For more information on the Rocksoft^tm Model CRC Algorithm, see the document
 * titled "A Painless Guide to CRC Error Detection Algorithms" by Ross
 * Williams (ross@guest.adelaide.edu.au.). This document is likely to be in
 * "ftp.adelaide.edu.au/pub/rocksoft"
 * Note: Rocksoft is a trademark of Rocksoft Pty Ltd, Adelaide, Australia.
 */
package net.jacksum.algorithms.crcs;

import java.security.NoSuchAlgorithmException;
import org.n16n.sugar.util.ByteSequences;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class CrcGeneric extends AbstractChecksum {

    protected long value;      // the value, must be accessed by subclasses
    private long poly;         // The algorithm's polynomial which is specified without its top bit
    private long initialValue; // Initial register value
    private boolean refIn;     // Reflect input bytes?
    private boolean refOut;    // Reflect output CRC?
    private long xorOut;       // XOR this to output CRC
    private long[] table;      // Precomputed values
    private long topBit;       // Stores the value (2 ^ width)
    private long maskAllBits;  // Stores the value (2 ^ width) - 1
    private long maskHelp;     // Stores the value (2 ^ (width-8)) -1
    private boolean includeLength = false; // include the length
    private boolean includeLengthMSOfirst = true; // include the length with the most significant octet first
    private boolean xorLength = false; // XOR the length
    private byte[] xorLengthArray; // the XOR array

    /**
     * Constructor with all parameters as defined in the
     * Rocksoft^tm Model CRC Algorithm
     * @param initialValue the initial register value
     * @param width width in bits
     * @param poly The algorithm's polynomial (without the highest bit)
     * @param refIn Reflect input bytes?
     * @param refOut Reflect output CRC?
     * @param xorOut XOR this to output CRC
     * @throws java.security.NoSuchAlgorithmException if the parameter cannot be used to create a correct object
     */
    public CrcGeneric(int width, long poly,
            long initialValue, boolean refIn,
            boolean refOut, long xorOut) throws NoSuchAlgorithmException {
        super();
        commonParamsInit(width, poly, initialValue, refIn, refOut, xorOut);
        init();
    }

    public CrcGeneric(int width, long poly,
            long initialValue, boolean refIn,
            boolean refOut, long xorOut, boolean includeLengthLTR) throws NoSuchAlgorithmException {
        super();
        commonParamsInit(width, poly, initialValue, refIn, refOut, xorOut);
        includeLengthInit(includeLengthLTR);
        init();
    }

    public CrcGeneric(int width, long poly,
            long initialValue, boolean refIn,
            boolean refOut, long xorOut, boolean includeLengthLTR,
            byte[] xorLengthArray) throws NoSuchAlgorithmException {
        super();
        commonParamsInit(width, poly, initialValue, refIn, refOut, xorOut);
        includeLengthInit(includeLengthLTR);
        xorLengthArrayInit(xorLengthArray);
        init();
    }

    private void xorLengthArrayInit(byte[] xorLengthArray) {
        this.xorLength = true;
        this.xorLengthArray = xorLengthArray;
    }

    private void includeLengthInit(boolean includeLengthMSO) {
        this.includeLength = true;
        this.includeLengthMSOfirst = includeLengthMSO;
    }

    private void commonParamsInit(int width, long poly,
            long initialValue, boolean refIn,
            boolean refOut, long xorOut) {
        formatPreferences.setHashEncoding(Encoding.DEC);
        this.bitWidth = width;
        this.poly = poly;
        this.initialValue = initialValue;
        this.refIn = refIn;
        this.refOut = refOut;
        this.xorOut = xorOut;
    }

    /*
     * Get the default encoding for the CrcGeneric
     * @return the default encoding


    @Override
    public Encoding getDefaultEncoding() {
        // set the default encoding explicitly, otherwise the output 
        // which is returned by getValueFormatted() could be negative if width is 64 bit
        return Encoding.DEC;
    }
*/
    /**
     * Constructor with a String parameter
     * @param props All parameters as defined in the Rocksoft^tm Model CRC Algorithm separated by a comma
     *        Example: crc:32,04C11DB7,FFFFFFFF,true,true,FFFFFFFF[,false,FFFFFFFF]
     * @throws java.security.NoSuchAlgorithmException if the parameter cannot be used to create a correct object
     */
    public CrcGeneric(String props) throws NoSuchAlgorithmException {
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
            bitWidth = Integer.parseInt(array[0]);
            poly = new java.math.BigInteger(array[1], 16).longValue();  //Long.parseLong(array[1], 16);
            initialValue = new java.math.BigInteger(array[2], 16).longValue(); //Long.parseLong(array[2], 16);
            refIn = array[3].equalsIgnoreCase("true");
            refOut = array[4].equalsIgnoreCase("true");
            xorOut = new java.math.BigInteger(array[5], 16).longValue();
            // 7th and 8th parameter (array index 6 and 7) are optional
            if (array.length >= 7) {
                includeLength = true;
                includeLengthMSOfirst = array[6].equalsIgnoreCase("true");
                if (array.length >= 8) {
                    xorLength = true;
                    xorLengthArray = hextext2bytes(array[7]);                    
                }
            }
        } catch (NumberFormatException e) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. " + e);
        } catch (IllegalArgumentException iae) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameter. "+iae.getMessage());
        }
        init();
    }

    private static byte[] hextext2bytes(String text) throws IllegalArgumentException {
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

    /**
     * Initializes the table, and a few other useful values
     */
    private void init() throws NoSuchAlgorithmException {
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
       
        topBit = 1L << (bitWidth - 1);       // stores the value (2 ^ width)
        maskAllBits = ~0L >>> (64 - bitWidth); // stores the value (2 ^ width) - 1
        maskHelp = maskAllBits >>> 8;     // stores the value (2 ^ (width-8)) -1
        check();
        fillTable();
        reset();
    }

    /**
     * Checks the parameters of the object
     */
    private void check() throws NoSuchAlgorithmException {
        if (bitWidth < 8 || bitWidth > 64) {
            throw new NoSuchAlgorithmException("Error: width has to be in range [8..64].");
        }

        if (poly != (poly & maskAllBits)) {
            throw new NoSuchAlgorithmException("Error: invalid polynomial for the " + bitWidth + " bit CRC.");
        }

        if (initialValue != (initialValue & maskAllBits)) {
            throw new NoSuchAlgorithmException("Error: invalid init value for the " + bitWidth + " bit CRC.");
        }
    }

    /**
     * Resets the checksum object to its initial values for further use
     */
    @Override
    public void reset() {
        length = 0;
        // Load the register with an initial value.
        value = initialValue;

        if (refIn) {
            // Reflect the initial value.
            value = reflect(value, bitWidth);
        }
    }

    @Override
    public int getSize() {
        return bitWidth;
    }

    /**
     * The toString() method is derived from the AbstractChecksum
     * @return a String which is understood by the constructor
     */
    public String getString() {
        StringBuilder sb = new StringBuilder();
        sb.append("crc:");
        int nibbles = bitWidth / 4 + ((bitWidth % 4 > 0) ? 1 : 0);
        sb.append(bitWidth);
        sb.append(",");
        sb.append(ByteSequences.hexformat(poly, nibbles).toUpperCase());
        sb.append(",");
        sb.append(ByteSequences.hexformat(initialValue, nibbles).toUpperCase());
        sb.append(",");
        sb.append(refIn ? "true" : "false");
        sb.append(",");
        sb.append(refOut ? "true" : "false");
        sb.append(",");
        sb.append(ByteSequences.hexformat(xorOut, nibbles).toUpperCase());
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
    
    public boolean isIncludeLength() {
        return includeLength;
    }
    
    public boolean isXorLength() {
        return xorLength;
    }
    
    public boolean isIncludeLengthMSOfirst() {
        return includeLengthMSOfirst;
    }
    
    public byte[] getXorLengthArray() {
        if (xorLength) {
            return xorLengthArray;
        } else {
            return null;
        }
            
    }

    /**
     * Get the name of the algorithm
     * @return the name of the algorithm as String
     */
    @Override
    public String getName() {
        if (name == null) {
            return getString();
        } else {
            return name;
        }
    }

    /**
     * Set the initial register value
     * @param initialValue the initial register value
     */
    public void setInitialValue(long initialValue) {
        this.initialValue = initialValue;
    }

    /**
     * Get the initial register value
     * @return the initial register value
     */
    public long getInitialValue() {
        return initialValue;
    }

    /**
     * Set the width in bits
     * @param width the width in bits
     */
    public void setWidth(int width) {
        this.bitWidth = width;
    }

    /**
     * Get the width in bits
     * @return the width in bits
     */
    public int getWidth() {
        return bitWidth;
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
    public boolean getRefIn() {
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
    public boolean getRefOut() {
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
     * Reflects a value by taking the least significant bits into account
     * Example: reflect(0x3e23L, 3) ==> 0x3e26
     *          11111000100011 ==> 11111000100110
     *
     * @param value the value which should be reflected
     * @param bits the number of bits to be reflected
     * @return the value with the bottom bits [0,64] reflected.
     */
    private static long reflect(long value, int bits) {
        long temp = 0L;
        for (int i = 0; i < bits; i++) {
            temp <<= 1L;
            temp |= (value & 1L);
            value >>>= 1L;
        }
        return (value << bits) | temp;
    }

    /**
     * Precomputes all 256 values
     */
    private void fillTable() {
        long remainder;
        boolean mybit;
        table = new long[256];

        // perform binary long division, a bit at a time
        for (int dividend = 0; dividend < 256; dividend++) {

            // initialize the remainder
            remainder = (long) dividend;

            if (refIn) { // reflection?
                remainder = reflect(remainder, 8);
            }

            remainder <<= (bitWidth - 8);

            for (int bit = 0; bit < 8; bit++) {
                // try to divide the current data bit
                mybit = ((remainder & topBit) != 0);
                remainder <<= 1;
                if (mybit) {
                    remainder ^= poly;
                }
            }

            if (refIn) { // reflection?
                remainder = reflect(remainder, bitWidth);
            }
            // save the result in the table
            table[dividend] = (remainder & maskAllBits);
        }
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        // performance improvement:
        // if condition not in the loop, therefore two separate loops
        int index;
        // divide the byte by the polynomial
        if (refIn) {
            for (int i = offset; i < length + offset; i++) {
                // Compute the index into the precomputed CRC table.
                index = ((int) (value ^ bytes[i]) & 0xff);

                // Slide the value a full byte to the right.
                value >>>= 8;

                // Clear the bits of the highest byte of the value.
                value &= maskHelp;

                // xor the value with the appropriate precomputed table value
                value ^= (table[index]);
            }
        } else {
            for (int i = offset; i < length + offset; i++) {
                // Compute the index into the precomputed CRC table.
                index = ((int) ((value >>> (bitWidth - 8)) ^ bytes[i]) & 0xff);

                // Slide the value a full byte to the left.
                value <<= 8;
                
                // xor the value with the appropriate precomputed table value
                value ^= (table[index]);
            }
        }
        this.length += length;
    }

    /**
     * Returns the value of the checksum
     * @return the value of the checksum
     */
    @Override
    public long getValue() {
        return getFinal();
    }

    /**
     * Returns the CRC value for the input processed so far
     * @return the CRC value for the input processed so far
     */
    protected long getFinal() {
        long valueSaved = value; // save the value
        long lengthSaved = length;

        // include the length of the file to the checksum value
        if (includeLength) {
            int numberOfBytesRequired = 0;
            for (; length != 0; length >>= 8) {
                numberOfBytesRequired++;
            }
            // count the number of required bytes
            // array represents the length as binary
            byte[] array = new byte[numberOfBytesRequired];

            // restore the length value, we need it unmodified again
            length = lengthSaved;

            // store the length with the most significant octet first
            for (int i = 0; length != 0; length >>= 8, i++) {
                array[array.length - 1 - i] = (byte) (length & 0xFF);
            }

            // we only want the length, but no xor'ing
            if (!xorLength) {
                // we want the least significant octet first
                if (!includeLengthMSOfirst) {
                    reverse(array);
                }
                update(array);

            } else { // we want the length, but xor'ing it first

                // allocate the size we need
                byte[] output = new byte[xorLengthArray.length];
                // put the length to the right in output

                // 010203040506  6  array
                //     01020304  4  output
                int srcPos = 0;
                if (array.length > output.length) {
                    srcPos = array.length - output.length;
                }
                //    010203  3  array
                //  ABCDEFGH  4  output
                int destPos = 0;
                if (array.length < output.length) {
                    destPos = output.length - array.length;
                }
                int min = Math.min(array.length, output.length);
                System.arraycopy(array, srcPos, output, destPos, min);

                // xor'ing the length value
                for (int i = 0; i < xorLengthArray.length; i++) {
                    output[i] ^= (xorLengthArray[i] & 0xFF);
                }

                if (!includeLengthMSOfirst) {
                    reverse(output);
                }
                update(output);
            }
        }

        long remainder = value;
        // restore the value and the length
        value = valueSaved;
        length = lengthSaved;

        // is output reflection still necessary?
        if (refIn != refOut) {
            remainder = reflect(remainder, bitWidth);
        }
        // the final remainder is the CRC result
        return (remainder ^ xorOut) & maskAllBits;
    }

    // reverses the byte array
    //
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

    /**
     * Returns the result of the computation as byte array
     * @return the result of the computation as byte array
     */
    @Override
    public byte[] getByteArray() {
        long finalvalue = getFinal();
        byte[] array = new byte[bitWidth / 8 + ((bitWidth % 8 > 0) ? 1 : 0)];

        for (int i = array.length - 1; i > -1; i--) {
            array[i] = (byte) (finalvalue & 0xFF);
            finalvalue >>>= 8;
        }

        return array;
    }

    protected void setValue(long value) {
        this.value = value;
    }

    protected long getValueInternal() {
        return value;
    }

    protected long[] getTable() {
        return table;
    }
    
    protected String polyAsMathExpression() {
        return polyAsMathExpression(bitWidth, poly);
    }
    
    /**
     * Formats a poly dependent on width as a math expression
     * @param width the width of the poly
     * @param poly the poly stored in a Java long
     * @return a math formatted poly
     */
    public static String polyAsMathExpression(int width, long poly) {
        StringBuilder sb = new StringBuilder();
        // this is implicitly always set
        sb.append("x^").append(width);
        
        byte[] bytes = new byte[8];
        ByteSequences.setLongInByteArray(poly, bytes);
        String bits = ByteSequences.formatAsBits(bytes);
        
        int exponent;
        for (int i = 0; i < bits.length(); i++) {
            exponent = (64 - 1 - i);
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

}
