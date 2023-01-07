/*

 Sugar for Java 2.0.0
 Copyright (C) 2001-2023  Dipl.-Inf. (FH) Johann N. Löfflmann,
 All Rights Reserved, https://johann.loefflmann.net

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

// This code has been copied from the LigtningJ project and incorporated into
// the sugar project. Both are released under the LGPLv3 and later, see also
// https://github.com/lightningj-org/lightningj/blob/master/src/main/java/org/lightningj/util/ZBase32.java

/*************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public License   *
 *  (LGPL-3.0-or-later)                                                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *  Modifications by jonelo on June 10, 2021:                            *
 *  - changed package org.lightningj.util to net.loefflmann.sugar.encodings *
 *  - reformatted code using IntelliJ and fixed some javadoc typos       *
 *  - some minor code improvements                                       *
 *                                                                       *
 *************************************************************************/
package net.loefflmann.sugar.encodings;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Z-Base32 encoder/decoder utility class, implementing the specification
 * from http://philzimmermann.com/docs/human-oriented-base-32-encoding.txt.
 * <p>
 * Z-base32 is intended to be a more human friendly encoding than base32 and
 * is used in Lightning in for instance invoicing.
 * <p>
 * This code is very much influenced by the golang version in GitHub project tv42/zbase32.
 * <p>
 * Created by Philip Vendil on 2018-02-15.
 */
public class ZBase32 {

    private static final byte[] encodeMap = "ybndrfg8ejkmcpqxot1uwisza345h769".getBytes();
    private static final byte[] decodeMap = new byte[256];
    private static final int[] lookupTable = new int[]{0, 1, 1, 2, 2, 3, 4, 4, 5};


    static {
        // initialize decode map
        // for (int i = 0; i < decodeMap.length; i++) {
        //    decodeMap[i] = (byte) 0xff;
        // }
        Arrays.fill(decodeMap, (byte) 0xff);
        for (int i = 0; i < encodeMap.length; i++) {
            decodeMap[encodeMap[i]] = (byte) i;
        }
    }

    /**
     * Method to encode a byte[] array into z-base32 encoding.
     *
     * @param data the data to encode.
     * @return the z-base32 encoded data.
     */
    public static byte[] encode(byte[] data) {
        return encode(data, -1);
    }

    /**
     * Method to encode a byte[] array into z-base32 encoding when
     * the exact number of bits is known.
     *
     * @param data         the data to encode.
     * @param numberOfBits the number of bits to use in the input data.
     * @return the z-base32 encoded data.
     */
    public static byte[] encode(byte[] data, int numberOfBits) {
        ByteBuffer destBuffer = ByteBuffer.allocate(encodedLength(data.length));
        int resultSize = encode(data, destBuffer, numberOfBits);
        byte[] result = new byte[resultSize];
        destBuffer.get(result, 0, resultSize);
        return result;
    }

    /**
     * Method to encode a byte[] array into a z-base32 encoded string.
     *
     * @param data the data to encode.
     * @return a z-base32 encoded string.
     */
    public static String encodeToString(byte[] data) {
        return new String(encode(data));
    }

    /**
     * Method to encode a byte[] array into a z-base32 encoded string when
     * the exact number of bits is known.
     *
     * @param data         the data to encode.
     * @param numberOfBits the number of bits to use in the input data.
     * @return a z-base32 encoded string.
     */
    public static String encodeToString(byte[] data, int numberOfBits) {
        return new String(encode(data, numberOfBits));
    }

    /**
     * Method to decode a byte[] z-base32 encoded array into the original bytes when
     * the exact number of bits is known.
     *
     * @param data         the z-base32 data to decode.
     * @param numberOfBits the number of bits to use in the input data.
     * @return the original decode data.
     * @throws IllegalArgumentException if invalid z-base32 data was specificed.
     */
    public static byte[] decode(byte[] data, int numberOfBits) throws IllegalArgumentException {
        ByteBuffer destBuffer = ByteBuffer.allocate(decodedLength(data.length));
        int resultSize = decode(data, destBuffer, numberOfBits);
        byte[] result = new byte[resultSize];
        destBuffer.get(result, 0, resultSize);
        return result;
    }

    /**
     * Method to decode a  z-base32 encoded byte[] array into into the original bytes.
     *
     * @param data the z-base32 data to decode.
     * @return the original decode data.
     * @throws IllegalArgumentException if invalid z-base32 data was specificed.
     */
    public static byte[] decode(byte[] data) throws IllegalArgumentException {
        return decode(data, -1);
    }

    /**
     * Method to decode a byte[] z-base32 String into the original bytes when
     * the exact number of bits is known.
     *
     * @param data         the z-base32 string to decode.
     * @param numberOfBits the number of bits to use in the input data.
     * @return the original decode data.
     * @throws IllegalArgumentException if invalid z-base32 data was specificed.
     */
    public static byte[] decode(String data, int numberOfBits) throws IllegalArgumentException {
        byte[] dataBytes = data.getBytes();
        ByteBuffer destBuffer = ByteBuffer.allocate(decodedLength(dataBytes.length));
        int resultSize = decode(dataBytes, destBuffer, numberOfBits);
        byte[] result = new byte[resultSize];
        destBuffer.get(result, 0, resultSize);
        return result;
    }

    /**
     * Method to decode a  z-base32 encoded String into the original bytes.
     *
     * @param data the z-base32 string to decode.
     * @return the original decode data.
     * @throws IllegalArgumentException if invalid z-base32 data was specified.
     */
    public static byte[] decode(String data) throws IllegalArgumentException {
        return decode(data.getBytes(), -1);
    }

    /**
     * Method to encode the given data into specified ByteBuffer when number
     * of bits in data is known.
     *
     * @param data         the data to encode into z-base32 encoding.
     * @param outBuffer    the output ByteBuffer to write encoded data to. The buffer
     *                     must have a maximum encoded length capacity.
     * @param numberOfBits the number of bits of the data to encode, otherwise -1
     * @return the number of bytes encoded.
     */
    private static int encode(byte[] data, ByteBuffer outBuffer, int numberOfBits) {
        ByteBuffer in = ByteBuffer.wrap(data);

        int outPos = 0;
        for (int i = 0; i < numberOfBits || (numberOfBits < 0 && in.capacity() > 0); i += 5) {
            int b0 = in.get(0) & 0xff;
            int b1 = (byte) 0;
            int c;
            int offset = i % 8;

            if (in.capacity() > 1) {
                b1 = in.get(1) & 0xff;
            }

            if (offset < 4) {
                c = (b0 & (31 << (3 - offset))) >> (3 - offset);
            } else {
                c = (b0 & (31 >> (offset - 3))) << (offset - 3);
                c |= (b1 & (255 << (11 - offset))) >> (11 - offset);
            }

            if (numberOfBits >= 0 && i + 5 > numberOfBits) {
                c &= 255 << (i + 5) - numberOfBits;
            }

            outBuffer.put(outPos, encodeMap[c]);
            outPos++;
            if (offset > 2) {
                in.position(1);
                in = in.slice();
            }
        }

        return outPos;
    }

    /**
     * Internal method to decode z-base32 encoded data to original bytes.
     *
     * @param data         the source bytes to decode
     * @param outBuffer    the byte buffer with max decoded length capacity.
     * @param numberOfBits the number of bits in source data, use -1 if not known.
     * @return the number of bytes in outBuffer that is written
     * @throws IllegalArgumentException if source bytes was invalid.
     */
    private static int decode(byte[] data, ByteBuffer outBuffer, int numberOfBits) throws IllegalArgumentException {
        ByteBuffer in = ByteBuffer.wrap(data);
        int outPos = 0;
        while (in.capacity() > 0) {
            int[] buffer = new int[8];

            int i = 0;
            for (; i < 8; i++) {
                if (in.capacity() == 0) {
                    break;
                }
                int b0 = in.get() & 0xff;
                in = in.slice();
                buffer[i] = decodeMap[b0];
                if (buffer[i] == 0xff) {
                    throw new IllegalArgumentException("Invalid z-base32 input data. " + new String(data));
                }
            }

            outBuffer.put(      outPos,     (byte) (buffer[0] << 3 | buffer[1] >> 2));
            outBuffer.put(outPos + 1, (byte) (buffer[1] << 6 | buffer[2] << 1 | buffer[3] >> 4));
            outBuffer.put(outPos + 2, (byte) (buffer[3] << 4 | buffer[4] >> 1));
            outBuffer.put(outPos + 3, (byte) (buffer[4] << 7 | buffer[5] << 2 | buffer[6] >> 3));
            outBuffer.put(outPos + 4, (byte) (buffer[6] << 5 | buffer[7]));

            if (numberOfBits < 0) {
                outPos += lookupTable[i];
                continue;
            }
            int bitsInBlock = numberOfBits;
            if (bitsInBlock > 40) {
                bitsInBlock = 40;
            }
            outPos += (bitsInBlock + 7) / 8;
            numberOfBits -= 40;
        }
        return outPos;
    }

    /**
     * Returns the maximum length in bytes for the encoded z-base32 version.
     *
     * @param dataLength the length of the original data.
     * @return the maximum encoded length
     */
    private static int encodedLength(int dataLength) {
        return (dataLength + 4) / 5 * 8;
    }

    /**
     * Returns the maximum length in bytes for the decoded data.
     *
     * @param dataLength the length of the z-base32 encoded data.
     * @return the maximum decoded length
     */
    private static int decodedLength(int dataLength) {
        return (dataLength + 7) / 8 * 5;
    }

}
