/*

 Sugar for Java 1.6.0
 Copyright (C) 2001-2021  Dipl.-Inf. (FH) Johann N. Löfflmann,
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

 */

package org.n16n.sugar.encodings;

import java.io.*;

/**
 * Routines for converting between Strings of base32-encoded data and arrays of
 * binary data. This currently supports the base32 and base32hex alphabets
 * specified in RFC 4648, sections 6 and 7.
 *
 * @author Brian Wellington
 * Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)
 * based on base32.java (released under the 2-clause BSD license)
 *
 * @author Johann N. Loefflmann
 * - added inner classes Padding, UpperLower
 * - added methods toString() and main(String[])
 * - replaced the methods blockLenToPadding(int) and paddingToBlockLen(int)
 *   with direct array access (performance improvement)
 * 
 */
public class Base32 {

    public static class Alphabet {

        private Alphabet() {
        }

        public static final String BASE32
                = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=";
        public static final String BASE32HEX
                = "0123456789ABCDEFGHIJKLMNOPQRSTUV=";
    }

    public static class Padding {

        private Padding() {
        }

        public static final boolean PADDING = true;
        public static final boolean NO_PADDING = false;
    }

    public static class UpperLower {

        private UpperLower() {
        }

        public static final boolean LOWERCASE = true;
        public static final boolean UPPERCASE = false;
    }

    private final String alphabet;
    private final boolean padding;
    private final boolean lowercase;

    private final static int[] blockLenToPadLen = { -1, 6, 4, 3, 1, 0 };
    private final static int[] padLenToBlockLen = { 5, 4, -1, 3, 2, -1, 1};

    /**
     * Creates an object that can be used to do base32 conversions.
     *
     * @param alphabet Which alphabet should be used
     * @param padding Whether padding should be used
     * @param lowercase Whether lowercase characters should be used. default
     * parameters (The standard base32 alphabet, no padding, uppercase)
     */
    public Base32(String alphabet, boolean padding, boolean lowercase) {
        this.alphabet = alphabet;
        this.padding = padding;
        this.lowercase = lowercase;
    }

    public Base32(String alphabet, boolean padding) {
        this(alphabet, padding, UpperLower.UPPERCASE);
    }

    public Base32(String alphabet) {
        this(alphabet, Padding.PADDING, UpperLower.UPPERCASE);
    }

    public Base32() {
        this(Alphabet.BASE32, Padding.PADDING, UpperLower.UPPERCASE);
    }

    /*
    private static int blockLenToPadding(int blocklen) {

        switch (blocklen) {
            case 1:
                return 6;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 1;
            case 5:
                return 0;
            default:
                return -1;
        }

    }
    */

    /*
    private static int paddingToBlockLen(int padlen) {

        switch (padlen) {
            case 6:
                return 1;
            case 4:
                return 2;
            case 3:
                return 3;
            case 1:
                return 4;
            case 0:
                return 5;
            default:
                return -1;
        }

    }
    */

    /**
     * Convert binary data to a base32-encoded String
     *
     * @param b An array containing binary data
     * @return A String containing the encoded data
     */
    public String encode(byte[] b) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (int i = 0; i < (b.length + 4) / 5; i++) {
            short[] s = new short[5];
            int[] t = new int[8];

            int blocklen = 5;
            for (int j = 0; j < 5; j++) {
                if ((i * 5 + j) < b.length) {
                    s[j] = (short) (b[i * 5 + j] & 0xFF);
                } else {
                    s[j] = 0;
                    blocklen--;
                }
            }
            //int padlen = blockLenToPadding(blocklen);
            int padlen = blockLenToPadLen[blocklen];

            // convert the 5 byte block into 8 characters (values 0-31).
            // see also the illustrations at
            // https://tools.ietf.org/html/rfc4648#section-9 and
            // https://de.wikipedia.org/wiki/Base32
            // upper 5 bits from first byte
            t[0] = (byte) ((s[0] >> 3) & 0x1F); // 0b00011111
            // lower 3 bits from 1st byte, upper 2 bits from 2nd.
            t[1] = (byte) (((s[0] & 0x07) << 2) | ((s[1] >> 6) & 0x03));
            // bits 5-1 from 2nd.
            t[2] = (byte) ((s[1] >> 1) & 0x1F);
            // lower 1 bit from 2nd, upper 4 from 3rd
            t[3] = (byte) (((s[1] & 0x01) << 4) | ((s[2] >> 4) & 0x0F));
            // lower 4 from 3rd, upper 1 from 4th.
            t[4] = (byte) (((s[2] & 0x0F) << 1) | ((s[3] >> 7) & 0x01));
            // bits 6-2 from 4th
            t[5] = (byte) ((s[3] >> 2) & 0x1F);
            // lower 2 from 4th, upper 3 from 5th;
            t[6] = (byte) (((s[3] & 0x03) << 3) | ((s[4] >> 5) & 0x07));
            // lower 5 from 5th;
            t[7] = (byte) (s[4] & 0x1F);

            // write out the actual characters.
            for (int j = 0; j < t.length - padlen; j++) {
                char c = alphabet.charAt(t[j]);
                if (lowercase) {
                    c = Character.toLowerCase(c);
                }
                os.write(c);
            }

            // write out the padding (if any)
            if (padding) {
                for (int j = t.length - padlen; j < t.length; j++) {
                    os.write('=');
                }
            }
        }
        return new String(os.toByteArray());
    }

    /**
     * Convert a base32-encoded String to binary data
     *
     * @param str A String containing the encoded data
     * @return An array containing the binary data, or null if the string is
     * invalid
     */
    public byte[] decode(String str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] raw = str.getBytes();
        for (int i = 0; i < raw.length; i++) {
            char c = (char) raw[i];
            if (!Character.isWhitespace(c)) {
                c = Character.toUpperCase(c);
                baos.write((byte) c);
            }
        }

        if (padding) {
            if (baos.size() % 8 != 0) {
                return null;
            }
        } else {
            while (baos.size() % 8 != 0) {
                baos.write('=');
            }
        }

        byte[] in = baos.toByteArray();

        baos.reset();
        DataOutputStream daos = new DataOutputStream(baos);

        for (int i = 0; i < in.length / 8; i++) {
            short[] s = new short[8];
            int[] t = new int[5];

            int padlen = 8;
            for (int j = 0; j < 8; j++) {
                char c = (char) in[i * 8 + j];
                if (c == '=') {
                    break;
                }
                s[j] = (short) alphabet.indexOf(in[i * 8 + j]);
                if (s[j] < 0) {
                    return null;
                }
                padlen--;
            }
            // int blocklen = paddingToBlockLen(padlen);
            int blocklen = padLenToBlockLen[padlen];
            if (blocklen < 0) {
                return null;
            }

            // all 5 bits of 1st, high 3 (of 5) of 2nd
            t[0] = (s[0] << 3) | s[1] >> 2;
            // lower 2 of 2nd, all 5 of 3rd, high 1 of 4th
            t[1] = ((s[1] & 0x03) << 6) | (s[2] << 1) | (s[3] >> 4);
            // lower 4 of 4th, high 4 of 5th
            t[2] = ((s[3] & 0x0F) << 4) | ((s[4] >> 1) & 0x0F);
            // lower 1 of 5th, all 5 of 6th, high 2 of 7th
            t[3] = (s[4] << 7) | (s[5] << 2) | (s[6] >> 3);
            // lower 3 of 7th, all of 8th
            t[4] = ((s[6] & 0x07) << 5) | s[7];

            try {
                for (int j = 0; j < blocklen; j++) {
                    daos.writeByte((byte) (t[j] & 0xFF));
                }
            } catch (IOException e) {
            }
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return String.format("alphabet=%s padding=%s lowercase=%s", alphabet, padding, lowercase);
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("String parameter expected.");
            return;
        }
        // Testvectors at https://tools.ietf.org/html/rfc4648#section-10
        Base32 base32_Padding = new Base32(Alphabet.BASE32, Padding.PADDING);
        Base32 base32_NoPadding = new Base32(Alphabet.BASE32, Padding.NO_PADDING);
        Base32 base32hex_Padding = new Base32(Alphabet.BASE32HEX, Padding.PADDING);
        Base32 base32hex_NoPadding = new Base32(Alphabet.BASE32HEX, Padding.NO_PADDING);

        Base32[] array = {base32_Padding, base32_NoPadding, base32hex_Padding, base32hex_NoPadding};

        for (Base32 base32 : array) {
            System.out.println(base32);
            String encoded = base32.encode(args[0].getBytes());
            System.out.printf("BASE32(\"%s\") = %s\n", args[0], encoded);
            byte[] decoded = base32.decode(encoded);
            System.out.println("Decoded: " + new String(decoded));
        }
    }

}
