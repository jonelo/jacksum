/*

  Sugar for Java 1.6.0
  Copyright (c) 2001-2026  Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, https://johann.loefflmann.net

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  @author Johann N. Löfflmann
 *
*/
package net.loefflmann.sugar.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Provides helper methods to convert texts to byte sequences and byte sequences to
 * texts, and to convert between numbers and byte arrays.
 */
public class ByteSequences {

    /**
     * Creates a new ByteSequences.
     */
    public ByteSequences() {
    }

    private final static char[] HEX = "0123456789abcdef".toCharArray();
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Transforms a text that contains hex numbers, separated by commas and/or whitespace,
     * to a byte array. Each pair of hex digits becomes one byte, a number with an odd
     * number of digits is prefixed with a 0, and an empty value becomes a 0 byte.
     *
     * @param text the text with the hex numbers
     * @return the byte array
     * @throws IllegalArgumentException if a value is not a hex number
     */
    public static byte[] hexText2Bytes(String text) throws IllegalArgumentException {
        if (text.length() == 0) {
            return new byte[0]; // empty byte array with 0 members
        }
        String[] tokens = text.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", ",").split(",");
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        try {
            for (String token : tokens) {
                if (token.length() == 0) {
                    token = "00";
                }
                int prefix = token.length() % 2;
                if (prefix > 0) {
                    token = "0" + token;
                }
                for (int i = 0; i < token.length();) {
                    String str = token.substring(i, i += 2);
                    byteArrayList.add((byte) Integer.parseInt(str, 16));
                }
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Not a hex number. " + nfe.getMessage());
        }
        return toByteArray(byteArrayList);
    }

    /**
     * Transforms a text that contains octal numbers, separated by commas and/or whitespace,
     * to a byte array. Each number becomes one byte, an empty value becomes a 0 byte.
     *
     * @param text the text with the octal numbers
     * @return the byte array
     * @throws IllegalArgumentException if a value is not an octal number, or if it is out
     *                                  of the range of one byte
     */
    public static byte[] octText2Bytes(String text) throws IllegalArgumentException {
        return numberText2Bytes(text, 8, " is not an octal number.");
    }

    /**
     * Transforms a text that contains numbers of the given radix, separated by commas
     * and/or whitespace, to a byte array.
     *
     * An empty value keeps its position and becomes a 0 byte, and a separator at the end
     * of the text does not add a byte, exactly as hexText2Bytes() and binText2Bytes()
     * handle it.
     *
     * @param text the text with the numbers
     * @param radix the radix of the numbers, e.g. 10 for decimal or 8 for octal
     * @param notANumberMessage the message that is appended to a token which is not a
     *                          number of that radix
     * @return the byte array
     * @throws IllegalArgumentException if a value is not a number of that radix, or if it
     *                                  is out of the range of one byte
     */
    private static byte[] numberText2Bytes(String text, int radix, String notANumberMessage)
            throws IllegalArgumentException {
        if (text.length() == 0) {
            return new byte[0]; // empty byte array with 0 members
        }
        String[] tokens = text.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", ",").split(",");
        byte[] bytes = new byte[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.length() == 0) { // a missing value keeps its position
                bytes[i] = 0;
                continue;
            }
            int value;
            try {
                value = Integer.parseInt(token, radix);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(token + notANumberMessage);
            }
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("The number " + value + " is out of range.");
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }


    /**
     * Transforms a text that contains binary numbers, separated by commas and/or
     * whitespace, to a byte array. Each group of 8 bits becomes one byte, a number whose
     * length is not a multiple of 8 is prefixed with zeros, and an empty value becomes a
     * 0 byte.
     *
     * @param text the text with the binary numbers
     * @return the byte array
     * @throws IllegalArgumentException if a value is not a binary number
     */
    public static byte[] binText2Bytes(String text) throws IllegalArgumentException {
        if (text.length() == 0) {
            return new byte[0]; // empty byte array with 0 members        
        }
        String[] tokens = text.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", ",").split(",");
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        try {
            for (String token : tokens) {
                if (token.length() == 0) {
                    token = "0";
                }
                // prefix token with leading zeros
                int prefix = token.length() % 8;
                if (prefix > 0) {
                    token = "00000000".substring(prefix) + token;
                }
                for (int i = 0; i < token.length();) {
                    String str = token.substring(i, i += 8);
                    byteArrayList.add((byte) Integer.parseInt(str, 2));
                }
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Not a binary number: " + nfe.getMessage());
        }
        return toByteArray(byteArrayList);
    }

    private static byte[] toByteArray(ArrayList<Byte> byteArrayList) {
        int n = byteArrayList.size();
        byte[] byteArray = new byte[n];
        for (int i = 0; i < n; i++) {
            byteArray[i] = byteArrayList.get(i);
        }
        return byteArray;
    }

    /**
     * Encodes a text to bytes using the platform's default charset.
     *
     * @param text the text
     * @return the encoded bytes
     */
    public static byte[] text2Bytes(String text) {
        return text.getBytes();
    }

    /**
     * Interprets escape sequences in a text and returns the result as UTF-8 encoded
     * bytes.
     *
     * The escape sequences \t, \n, \r, \", \' and \\ are translated to the character
     * they stand for. The escape sequence \xHH is translated to the character with the
     * hexadecimal value HH, where HH can become 00 to 7F, and the two hex digits can be
     * written in both upper and lower case. Any other backslash sequence, an incomplete
     * \xHH sequence, and \x80 to \xFF are kept unchanged.
     *
     * The text is processed in one pass from left to right, so a translated character is
     * never interpreted as the start of another escape sequence, e.g. \\n stays a
     * backslash followed by an "n", and \\x41 stays a backslash followed by "x41".
     *
     * @param text a text, possibly with escape sequences.
     * @return the UTF-8 encoded bytes of the text with all escape sequences interpreted.
     */
    public static byte[] textf2Bytes(String text) {
        // jacksum -a none -q txtf:"\"\"\n" -F "hex=#SEQUENCE, length=#LENGTH"
        return GeneralString.translateEscapeSequences(text, true).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Transforms a text that contains decimal numbers, separated by commas and/or
     * whitespace, to a byte array. Each number becomes one byte, an empty value becomes
     * a 0 byte.
     *
     * @param text the text with the decimal numbers
     * @return the byte array
     * @throws IllegalArgumentException if a value is not a decimal number, or if it is
     *                                  out of the range of one byte
     */
    public static byte[] decText2Bytes(String text) throws IllegalArgumentException {
        return numberText2Bytes(text, 10, " is not a decimal number.");
    }

    /**
     * Formats a value as a lowercase hex string, left-padded with zeros to the specified
     * number of nibbles.
     *
     * @param value the value
     * @param nibbles the minimum number of hex digits
     * @return the hex string
     */
    public static String hexformat(long value, int nibbles) {
        StringBuilder sb = new StringBuilder(Long.toHexString(value));
        while (sb.length() < nibbles) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    /**
     * Formats a value as a lowercase hex string, left-padded with zeros to the specified
     * number of nibbles, and optionally grouped.
     *
     * @param value the value
     * @param nibbles the minimum number of hex digits
     * @param group the number of hex digits per group, or 0 for no grouping
     * @param groupChar the character that separates the groups
     * @return the hex string
     */
    public static String hexformat(long value, int nibbles, int group, char groupChar) {
        StringBuffer sb = new StringBuffer(Long.toHexString(value));
        while (sb.length() < nibbles) {
            sb.insert(0, '0');
        }
        if (group > 0) {
            sb = GeneralString.insertBlanks(sb, group, groupChar);
        }
        return sb.toString();
    }

    /**
     * Method transfers a long to 8 bytes and fills the array given in argument
     *
     * @param i a long value
     * @param b the byte array
     */
    public static void setLongInByteArray(long i, byte[] b) throws IndexOutOfBoundsException {
        setLongInByteArray(i, b, 0);
    }

    /**
     * Method transfers a long to 8 bytes and fills the array given in argument
     *
     * @param i a long value
     * @param b the byte array
     * @param index the index in the array
     */
    public static void setLongInByteArray(long i, byte[] b, int index) throws IndexOutOfBoundsException {
        byte[] b1 = new byte[8];
        long i1;
        for (int j = 0; j < 8; j++) {
            i1 = (i & 255);
            b1[j] = (byte) i1;
            i = i >> 8;
        }
        for (int j = 0; j < 8; j++) {
            b[j + index] = b1[7 - j];
        }
    }

    /**
     * Transforms a long to 8 bytes in big-endian order.
     *
     * @param l the long value
     * @return the 8 bytes
     */
    public static byte[] unsignedLongToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>>= Byte.SIZE;
        }
        return result;
    }

    /**
     * Transforms a long to 8 bytes in big-endian order.
     *
     * @param l the long value
     * @return the 8 bytes
     */
    public static byte[] signedLongToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    /**
     * Transforms an int to 4 bytes in big-endian order and writes them to the beginning
     * of the array given in argument.
     *
     * @param i an int value
     * @param b the byte array
     * @throws IndexOutOfBoundsException if the array is shorter than 4 bytes
     */
    public static void setIntInByteArray(int i, byte[] b) throws IndexOutOfBoundsException {
        setIntInByteArray(i, b, 0);
    }

    /**
     * Formats bytes as a lowercase hex string.
     *
     * @param bytes the bytes
     * @return the hex string, or an empty string if bytes is null
     */
    public static String format(byte[] bytes) {
        return format(bytes, false);
    }

    /**
     * Formats bytes as a string of bits, 8 bits per byte.
     *
     * @param bytes the bytes
     * @return the bit string, or an empty string if bytes is null or empty
     */
    public static String formatAsBits(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 8);
        BigInteger big = new BigInteger(1, bytes);
        sb.append(big.toString(2)); // dual
        while (sb.length() < (bytes.length * 8)) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }


    /**
     * Formats bytes as a string of exactly the specified number of bits. Surplus leading
     * bits are cut off, missing leading bits are filled with zeros.
     *
     * @param bytes the bytes
     * @param bits the number of bits
     * @return the bit string
     */
    public static String formatAsBits(byte[] bytes, int bits) {
        String bytesAsBits = formatAsBits(bytes);
        if (bytesAsBits.length() > bits) {
            return bytesAsBits.substring(bytesAsBits.length() - bits);
        } else
        if (bytesAsBits.length() < bits) {
            StringBuilder sb = new StringBuilder(bits);
            sb.append(bytesAsBits);
            while (sb.length() < bits) {
                sb.insert(0, '0');
            }
            return sb.toString();
        } else
        return bytesAsBits; // bytesAsBits.length() == bits)
    }

    /**
     * Formats bytes as a hex string, optionally grouped.
     *
     * @param bytes the bytes
     * @param uppercase if true, uppercase hex digits are used
     * @param group the number of hex digits per group, or 0 for no grouping
     * @param groupChar the character that separates the groups
     * @return the hex string, or an empty string if bytes is null
     */
    public static String format(byte[] bytes, boolean uppercase, int group, char groupChar) {
        if (bytes == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        int b;
        for (int i = 0; i < bytes.length; i++) {
            b = bytes[i] & 255;
            sb.append(HEX[b >>> 4]);
            sb.append(HEX[b & 15]);
        }
        if (group > 0) {
            sb = GeneralString.insertBlanks(sb, group, groupChar);
        }
        return uppercase ? sb.toString().toUpperCase(Locale.US) : sb.toString();
    }

    /**
     * Formats bytes as a hex string.
     *
     * @param bytes the bytes
     * @param uppercase if true, uppercase hex digits are used
     * @return the hex string, or an empty string if bytes is null
     */
    public static String format(byte[] bytes, boolean uppercase) {
        return format(bytes, uppercase, 0, ' ');
    }

    /**
     * Method transforms an int to 4 bytes and fills the array given in argument
     *
     * @param i a int value
     * @param b the byte array
     * @param index the index in the array
     */
    public static void setIntInByteArray(int i, byte[] b, int index) throws IndexOutOfBoundsException {
        byte[] b1 = new byte[4];
        int i1;
        for (int j = 0; j < 4; j++) {
            i1 = (i & 255);
            b1[j] = (byte) i1;
            i = i >> 8;
        }
        for (int j = 0; j < 4; j++) {
            b[j + index] = b1[3 - j];
        }
    }

    /**
     * Transforms the lowest 4 bits of a value to an uppercase hex digit.
     *
     * @param nibble the value, only the lowest 4 bits are used
     * @return the hex digit
     */
    public static char nibbleToHexChar(int nibble) {
        return hexDigits[(nibble & 15)];
    }

    
    /**
     * Transforms 4 bytes in big-endian order to an int.
     *
     * @param bytes the 4 bytes
     * @return the int value
     * @throws IllegalArgumentException if the array does not have exactly 4 bytes
     */
    public static int fourByteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) throw new IllegalArgumentException();
     return ((bytes[0] & 0xFF) << 24) | 
            ((bytes[1] & 0xFF) << 16) | 
            ((bytes[2] & 0xFF) << 8 ) | 
            ((bytes[3] & 0xFF));
    }
     
    /**
     * Transforms 2 bytes in big-endian order to an int.
     *
     * @param bytes the 2 bytes
     * @return the int value
     * @throws IllegalArgumentException if the array does not have exactly 2 bytes
     */
    public static int twoByteArrayToInt(byte[] bytes) {
        if (bytes.length != 2) throw new IllegalArgumentException();
    
        return ((bytes[0] & 0xFF) << 8 ) | 
               ((bytes[1] & 0xFF));
    }

    
}
