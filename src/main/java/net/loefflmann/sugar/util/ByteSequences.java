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
import java.util.StringTokenizer;

public class ByteSequences {

    private final static char[] HEX = "0123456789abcdef".toCharArray();
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

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

    public static byte[] octText2Bytes(String text) throws IllegalArgumentException {
        if (text.length() == 0) {
            return new byte[0]; // empty byte array with 0 members
        }
        text = text.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", ",");
        byte[] bytes;
        if (text.length() == 0) {
            bytes = text.getBytes();
        } else {
            int count = GeneralString.countChar(text, ',');
            bytes = new byte[count + 1];
            StringTokenizer st = new StringTokenizer(text, ",");
            int x = 0;
            while (st.hasMoreTokens()) {
                int temp;
                String stemp = null;
                try {
                    stemp = st.nextToken();
                    temp = Integer.parseInt(stemp,8);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(stemp + " is not an octal number.");
                }
                if (temp < 0 || temp > 255) {
                    throw new IllegalArgumentException("The number " + temp + " is out of range.");
                }
                bytes[x++] = (byte) temp;
            }
        }
        return bytes;
    }


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

    public static byte[] decText2Bytes(String text) throws IllegalArgumentException {
        if (text.length() == 0) {
            return new byte[0]; // empty byte array with 0 members
        }
        text = text.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", ",");
        byte[] bytes;
        if (text.length() == 0) {
            bytes = text.getBytes();
        } else {
            int count = GeneralString.countChar(text, ',');
            bytes = new byte[count + 1];
            StringTokenizer st = new StringTokenizer(text, ",");
            int x = 0;
            while (st.hasMoreTokens()) {
                int temp;
                String stemp = null;
                try {
                    stemp = st.nextToken();
                    temp = Integer.parseInt(stemp);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(stemp + " is not a decimal number.");
                }
                if (temp < 0 || temp > 255) {
                    throw new IllegalArgumentException("The number " + temp + " is out of range.");
                }
                bytes[x++] = (byte) temp;
            }
        }
        return bytes;
    }

    public static String hexformat(long value, int nibbles) {
        StringBuilder sb = new StringBuilder(Long.toHexString(value));
        while (sb.length() < nibbles) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

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

    public static byte[] unsignedLongToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>>= Byte.SIZE;
        }
        return result;
    }

    public static byte[] signedLongToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static void setIntInByteArray(int i, byte[] b) throws IndexOutOfBoundsException {
        setIntInByteArray(i, b, 0);
    }

    public static String format(byte[] bytes) {
        return format(bytes, false);
    }

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

    public static char nibbleToHexChar(int nibble) {
        return hexDigits[(nibble & 15)];
    }

    
    public static int fourByteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) throw new IllegalArgumentException();
     return ((bytes[0] & 0xFF) << 24) | 
            ((bytes[1] & 0xFF) << 16) | 
            ((bytes[2] & 0xFF) << 8 ) | 
            ((bytes[3] & 0xFF));
    }
     
    public static int twoByteArrayToInt(byte[] bytes) {
        if (bytes.length != 2) throw new IllegalArgumentException();
    
        return ((bytes[0] & 0xFF) << 8 ) | 
               ((bytes[1] & 0xFF));
    }

    
}
