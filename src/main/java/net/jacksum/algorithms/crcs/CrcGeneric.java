/*


  Jacksum 3.5.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 * titled "A Painless Guide to CRC Error Detection Algorithms" written by Ross
 * Williams in 1993.
 *
 * The original links are gone meanwhile, but you can still find the paper
 * on Dr. Ross Williams' homepage at
 * http://www.ross.net/crc/crcpaper.html
 *
 * Note: Rocksoft was a trademark of Rocksoft Pty Ltd, Adelaide, Australia.
 * Ross Williams founded Rocksoft which was sold to ADIC (now Quantum) in 2006.
 *
 */
package net.jacksum.algorithms.crcs;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.zzadopt.com.github.snksoft.crc.CRC;

import java.security.NoSuchAlgorithmException;

public class CrcGeneric extends AbstractChecksum implements CrcInfo {

    protected long value;      // the value, must be accessed by subclasses
    private long[] table;      // Precomputed values
    private long topBit;       // Stores the value (2 ^ width)
    private long maskAllBits;  // Stores the value (2 ^ width) - 1
    private long maskHelp;     // Stores the value (2 ^ (width-8)) -1
    private CrcModelExtended model;
    private CRC tableDriven;

    /**
     * Constructor with all parameters as defined in the
     * Rocksoft^tm Model CRC Algorithm
     *
     * @param width        width in bits
     * @param poly         The algorithm's polynomial (without the highest bit)
     * @param initialValue the initial register value
     * @param refIn        Reflect input bytes?
     * @param refOut       Reflect output CRC?
     * @param xorOut       XOR this to output CRC
     * @throws NoSuchAlgorithmException if the parameter cannot be used to create a correct object
     */
    public CrcGeneric(int width, long poly, long initialValue, boolean refIn, boolean refOut, long xorOut)
            throws NoSuchAlgorithmException {
        super();
        model = new CrcModelExtended(width, poly, initialValue, refIn, refOut, xorOut);
        init();
    }

    public CrcGeneric(int width, long poly, long initialValue, boolean refIn, boolean refOut, long xorOut,
                      boolean includeLengthLTR) throws NoSuchAlgorithmException {
        super();
        model = new CrcModelExtended(width, poly, initialValue, refIn, refOut, xorOut, includeLengthLTR);
        init();
    }

    public CrcGeneric(int width, long poly, long initialValue, boolean refIn, boolean refOut, long xorOut,
                      boolean includeLengthLTR, byte[] xorLengthArray) throws NoSuchAlgorithmException {
        super();
        model = new CrcModelExtended(width, poly, initialValue, refIn, refOut, xorOut, includeLengthLTR, xorLengthArray);
        init();
    }

    public CrcGeneric(CrcModelExtended model) throws NoSuchAlgorithmException {
        super();
        this.model = model;
        init();
    }

    /**
     * Constructor with a String parameter
     *
     * @param props All parameters as defined in the Rocksoft^tm Model CRC Algorithm separated by a comma
     *              Example: crc:32,04C11DB7,FFFFFFFF,true,true,FFFFFFFF[,false,FFFFFFFF]
     * @throws NoSuchAlgorithmException if the parameter cannot be used to create a correct object
     */
    public CrcGeneric(String props) throws NoSuchAlgorithmException {
        model = new CrcModelExtended(props);
        init();
    }


    public CrcModelExtended getModel() {
        return model;
    }


    /**
     * Initializes the table, and a few other useful values
     */
    private void init() throws NoSuchAlgorithmException {
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
        bitWidth = model.getWidth();

        //topBit = 1L << (model.getWidth() - 1);       // stores the value (2 ^ width)
        maskAllBits = ~0L >>> (64 - model.getWidth()); // stores the value (2 ^ width) - 1
        //maskHelp = maskAllBits >>> 8;     // stores the value (2 ^ (width-8)) -1
        check();
        tableDriven = new CRC(model.getParameters());
        reset();
    }


    /**
     * Checks the parameters of the object
     */
    private void check() throws NoSuchAlgorithmException {
        if (model.getWidth() < 1 || model.getWidth() > 64) {
            throw new NoSuchAlgorithmException("Error: CRC parameter width has to be in range [1..64].");
        }

        if (model.getPoly() != (model.getPoly() & maskAllBits)) {
            throw new NoSuchAlgorithmException(String.format("Error: invalid polynomial for the %s bit CRC.", model.getWidth()));
        }

        if (model.getInit() != (model.getInit() & maskAllBits)) {
            throw new NoSuchAlgorithmException(String.format("Error: invalid init value for the %s bit CRC.", model.getWidth()));
        }
    }


    /**
     * Resets the checksum object to its initial values for further use
     */
    @Override
    public void reset() {
        length = 0;
        value = tableDriven.init();
    }


    @Override
    public int getSize() {
        return model.getWidth();
    }


    /**
     * The toString() method is derived from the AbstractChecksum
     *
     * @return a String which is understood by the constructor
     */
    public String getString() {
        return model.getString();
    }


    /**
     * Get the name of the algorithm
     *
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


    @Override
    public void update(byte[] bytes, int offset, int length) {
        value = tableDriven.update(value, bytes, offset, length);
        this.length += length;
    }

    /**
     * Returns the value of the checksum
     *
     * @return the value of the checksum
     */
    @Override
    public long getValue() {
        return getFinal();
    }

    // include the length of the file to the checksum value
    private long includeLength() {
        long valueSaved = value; // save the value
        long lengthSaved = length;

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
        if (!model.isXorLength()) {
            // we want the least significant octet first
            if (!model.isIncludeLengthMSOfirst()) {
                CrcUtils.reverse(array);
            }
            update(array);

        } else { // we want the length, but xor'ing it first

            // allocate the size we need
            byte[] output = new byte[model.getXorLengthArray().length];
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
            for (int i = 0; i < model.getXorLengthArray().length; i++) {
                output[i] ^= (model.getXorLengthArray()[i] & 0xFF);
            }

            if (!model.isIncludeLengthMSOfirst()) {
                CrcUtils.reverse(output);
            }
            update(output);
        }

        long newValue = value;

        value = valueSaved; // restore the saved value, so we can call update again even after getFinal() has been called
        length = lengthSaved; // restore the length, because we want the number of bytes processed

        return newValue;
    }

    /**
     * Returns the CRC value for the input processed so far
     *
     * @return the CRC value for the input processed so far
     */
    protected long getFinal() {
        return tableDriven.finalCRC(model.isIncludeLength() ? includeLength() : value);
    }


    /**
     * Returns the result of the computation as byte array
     *
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
        return CrcUtils.polyAsMathExpression(model.getWidth(), model.getPoly());
    }


    @Override
    public byte[] getPolyAsBytes() {
        long p = model.getPoly();
        byte[] array = new byte[bitWidth / 8 + ((bitWidth % 8 > 0) ? 1 : 0)];

        for (int i = array.length - 1; i > -1; i--) {
            array[i] = (byte) (p & 0xFF);
            p >>>= 8;
        }
        return array;
    }

    @Override
    public int getWidth() {
        return model.getWidth();
    }

    @Override
    public long getInitialValue() {
        return model.getInit();
    }

    @Override
    public boolean isRefIn() {
        return model.isRefIn();
    }

    @Override
    public boolean isRefOut() {
        return model.isRefOut();
    }

    @Override
    public long getXorOut() {
        return model.getXorOut();
    }

    public boolean isTainted() {
        return false;
    }
}
