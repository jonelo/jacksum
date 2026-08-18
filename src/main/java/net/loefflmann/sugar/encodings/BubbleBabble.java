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

 */
package net.loefflmann.sugar.encodings;

import java.util.Locale;

/*
  encode() has been ported to Java from the OpenSSH's C source called key.c by
  Johann N. Loefflmann

  See also http://web.mit.edu/kenta/www/one/bubblebabble/spec/jrtrjwzi/draft-huima-01.txt

 */
/**
 * Header notice from the source called key.c in OpenSSH:
 *
 * Copyright (c) 2000, 2001 Markus Friedl. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class BubbleBabble {

    private static final char[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};

    private static final char[] CONSONANTS = {'b', 'c', 'd', 'f', 'g', 'h', 'k', 'l', 'm',
        'n', 'p', 'r', 's', 't', 'v', 'z', 'x'};

    public static String encode(byte[] raw) {

        int seed = 1;
        int rounds = (raw.length / 2) + 1;
        StringBuilder retval = new StringBuilder(rounds * 6);
        retval.append('x');

        // x|abcd-e|abcd-e|fgh|x
        for (int i = 0; i < rounds; i++) {
            int idx0, idx1, idx2, idx3, idx4;

            if ((i + 1 < rounds) || ((raw.length % 2) != 0)) {

                idx0 = ((((((int) (raw[2 * i])) & 0xff) >>> 6) & 3) + seed) % 6;
                idx1 = ((((int) (raw[2 * i])) & 0xff) >>> 2) & 15;
                idx2 = (((((int) (raw[2 * i])) & 0xff) & 3) + (seed / 6)) % 6;

                retval.append(VOWELS[idx0]);
                retval.append(CONSONANTS[idx1]);
                retval.append(VOWELS[idx2]);

                if (i + 1 < rounds) {
                    idx3 = ((((int) (raw[2 * i + 1])) & 0xff) >>> 4) & 15;
                    idx4 = (((int) (raw[2 * i + 1])) & 0xff) & 15;

                    retval.append(CONSONANTS[idx3]);
                    retval.append('-');
                    retval.append(CONSONANTS[idx4]);
                    seed = ((seed * 5)
                            + (((((int) (raw[2 * i])) & 0xff) * 7)
                            + (((int) (raw[2 * i + 1])) & 0xff))) % 36;
                }
            } else {
                idx0 = seed % 6;
                idx1 = 16;
                idx2 = seed / 6;
                retval.append(VOWELS[idx0]);
                retval.append(CONSONANTS[idx1]);
                retval.append(VOWELS[idx2]);
            }
        }

        retval.append('x');
        return retval.toString();
    }

    /**
     * Method to decode a BubbleBabble encoded String into the original bytes.
     * It is the inverse operation of encode(). The redundancy that is part of
     * the BubbleBabble encoding is verified, so that both typos and transmission
     * errors are detected rather than silently decoded to arbitrary bytes.
     *
     * @param babble the BubbleBabble string to decode, both lower and upper case
     *               characters are supported.
     * @return the original decoded data.
     * @throws IllegalArgumentException if invalid BubbleBabble data was specified.
     */
    public static byte[] decode(String babble) throws IllegalArgumentException {
        if (babble == null) {
            throw new IllegalArgumentException("BubbleBabble decoding error: the input is null.");
        }
        // the BubbleBabble alphabet is not case sensitive
        String input = babble.toLowerCase(Locale.US);
        int length = input.length();

        // x|abcd-e|abcd-e|fgh|x, i. e. 6 chars per round, but 3 chars for the last
        // round, plus the two 'x' that frame the tuples: length = 6 * rounds - 1
        if (length < 5 || (length % 6) != 5) {
            throw new IllegalArgumentException(String.format(
                    "BubbleBabble decoding error: %s is not a valid length for a BubbleBabble string.", length));
        }
        if (input.charAt(0) != 'x' || input.charAt(length - 1) != 'x') {
            throw new IllegalArgumentException(
                    "BubbleBabble decoding error: the string must both start and end with an 'x'.");
        }

        int rounds = (length + 1) / 6;
        // The consonant in the middle of the last tuple is an 'x' if and only if the
        // number of the encoded bytes is even, because a tuple that carries data never
        // uses an 'x' at that position (see idx1 in encode(), it is 0 to 15, never 16).
        boolean even = input.charAt(6 * (rounds - 1) + 2) == 'x';
        byte[] raw = new byte[2 * (rounds - 1) + (even ? 0 : 1)];

        int seed = 1;
        for (int i = 0; i < rounds; i++) {
            int pos = 1 + (6 * i);

            if (i + 1 < rounds) {
                byte byte1 = tuple2byte(input, pos, seed);

                if (input.charAt(pos + 3 + 1) != '-') {
                    throw new IllegalArgumentException(String.format(
                            "BubbleBabble decoding error: a '-' was expected at index %s.", pos + 4));
                }
                byte byte2 = (byte) ((consonant2index(input.charAt(pos + 3)) << 4)
                        | consonant2index(input.charAt(pos + 5)));

                raw[2 * i] = byte1;
                raw[2 * i + 1] = byte2;

                seed = ((seed * 5)
                        + (((((int) byte1) & 0xff) * 7)
                        + (((int) byte2) & 0xff))) % 36;
            } else if (even) {
                // the last tuple does not carry data, it just encodes the seed
                if (vowel2index(input.charAt(pos)) != (seed % 6)
                        || vowel2index(input.charAt(pos + 2)) != (seed / 6)) {
                    throw new IllegalArgumentException(String.format(
                            "BubbleBabble decoding error: the checksum of the tuple at index %s does not match.", pos));
                }
            } else {
                raw[raw.length - 1] = tuple2byte(input, pos, seed);
            }
        }

        return raw;
    }

    /**
     * Decodes the three characters (vowel, consonant, vowel) at the given index
     * to the one byte that they carry.
     *
     * @param input the BubbleBabble string, in lower case.
     * @param pos   the index of the first of the three characters.
     * @param seed  the current value of the seed.
     * @return the decoded byte.
     * @throws IllegalArgumentException if the characters are not valid, or if the
     *                                  redundancy that is provided by the seed does not match.
     */
    private static byte tuple2byte(String input, int pos, int seed) throws IllegalArgumentException {
        int idx0 = vowel2index(input.charAt(pos));
        int idx1 = consonant2index(input.charAt(pos + 1));
        int idx2 = vowel2index(input.charAt(pos + 2));

        // the vowels carry two bits each, so both values have to be 0 to 3
        int high = ((idx0 - (seed % 6)) + 6) % 6;
        int low = ((idx2 - (seed / 6)) + 6) % 6;
        if (high > 3 || low > 3) {
            throw new IllegalArgumentException(String.format(
                    "BubbleBabble decoding error: the checksum of the tuple at index %s does not match.", pos));
        }
        return (byte) ((high << 6) | (idx1 << 2) | low);
    }

    /**
     * Returns the index of the given vowel in the BubbleBabble vowel alphabet.
     *
     * @param vowel the character to look up.
     * @return the index of the vowel, it is 0 to 5.
     * @throws IllegalArgumentException if the character is not a BubbleBabble vowel.
     */
    private static int vowel2index(char vowel) throws IllegalArgumentException {
        for (int i = 0; i < VOWELS.length; i++) {
            if (VOWELS[i] == vowel) {
                return i;
            }
        }
        throw new IllegalArgumentException(String.format(
                "BubbleBabble decoding error: '%s' is not a valid vowel.", vowel));
    }

    /**
     * Returns the index of the given consonant in the BubbleBabble consonant alphabet.
     * The 'x' is not accepted, because it never carries data.
     *
     * @param consonant the character to look up.
     * @return the index of the consonant, it is 0 to 15.
     * @throws IllegalArgumentException if the character is not a BubbleBabble consonant
     *                                  that carries data.
     */
    private static int consonant2index(char consonant) throws IllegalArgumentException {
        for (int i = 0; i < CONSONANTS.length - 1; i++) { // the last one is the 'x'
            if (CONSONANTS[i] == consonant) {
                return i;
            }
        }
        throw new IllegalArgumentException(String.format(
                "BubbleBabble decoding error: '%s' is not a valid consonant at this position.", consonant));
    }

}
