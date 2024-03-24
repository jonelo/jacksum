/*

  Jacksum 3.8.0 - a checksum utility in Java
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

package net.jacksum.algorithms.md;

/*
 * Original source (MIT license aka X11 license):
 * https://github.com/romulusae/romulus/blob/master/Implementations/software/ref/Romulus-H/hash.c
 *
 * Date: 21 April 2021
 * Contact: Romulus Team (Mustafa Khairallah - mustafa.khairallah@ntu.edu.sg)
 * Romulus-H as compliant with the Romulus v1.3 specifications.
 * This file icludes crypto_aead_decrypt()
 * It superseeds earlier versions developed by Mustafa Khairallah and maintained
 * by Mustafa Khairallah, Thomas Peyrin and Kazuhiko Minematsu
 *
 * Date: 24 March 2024
 * This source has been translated from C to Java by Johann N. Loefflmann and
 * it is released under the conditions of both the original MIT license and the
 * GPLv3+ - jonelo@jonelo.de
 */

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Romulus_H extends AbstractChecksum {

    private byte[] digest = new byte[32];
    protected boolean virgin = true;

    // two 16 byte arrays to store the internal state
    private byte[] h = new byte[16];
    private byte[] g = new byte[16];

    // byte array to store the last message that didn't fit in a full 32 byte array
    private byte[] lastMessage = new byte[32];
    private int lastMessageLength = 0;


    public static void skinny_128_384_plus_enc(byte[] input, byte[] userkey) {
        Skinny128_384_plus.enc(input, userkey);
    }

    // in = input message
    // inlen = length of the input message
    // out = the 256 bit hash
    public static void crypto_hash(byte[] out, byte[] in, long inlen) {
        byte[] h = new byte[16];
        byte[] g = new byte[16];
        long mlen = inlen;
        byte[] p = new byte[32];

        initialize(h, g);
        while (mlen >= 32) {
            hirose_128_128_256(h, g, in);
            in = Arrays.copyOfRange(in, 32, in.length);
            mlen -= 32;
        }

        ipad_256(in, p, 32, (int) mlen);
        h[0] ^= 2;
        hirose_128_128_256(h, g, p);

        System.arraycopy(h, 0, out, 0, 16);
        System.arraycopy(g, 0, out, 16, 16);
    }



    // The hirose double-block length (DBL) compression function.
    // works on the first 32 bytes in m(essage) only.
    public static void hirose_128_128_256(byte[] h, byte[] g, byte[] m) {
        byte[] key = new byte[48];
        byte[] hh = new byte[16];

        for (int i = 0; i < 16; i++) { // assign the key for the
            // hirose compressison function
            key[i] = g[i];
            g[i] = h[i];
            hh[i] = h[i];
        }

        g[0] ^= 0x01;

        for (int i = 0; i < 32; i++) {
            key[i + 16] = m[i];
        }

        skinny_128_384_plus_enc(h, key);
        skinny_128_384_plus_enc(g, key);

        for (int i = 0; i < 16; i++) {
            h[i] ^= hh[i];
            g[i] ^= hh[i];
        }

        g[0] ^= 0x01;
    }

    // Sets the initial value to 0^2n
    private static void initialize(byte[] h, byte[] g) {
        Arrays.fill(h, (byte) 0);
        Arrays.fill(g, (byte) 0);
    }


    // Padding function: pads the byte length of the message mod 32 to the last incomplete block.
    // For complete blocks it returns the same block. For an empty block it returns a 0^2n string.
    // The function is called for full block messages to add a 0^2n block. This and the modulus are
    // the only differences compared to the use in Romulus-N
    // m = message
    // mp = message, padded
    // l = length of the padded message
    // len8 = length of the message m, expressed in 8 bits
    //
    public static void ipad_256(byte[] m, byte[] mp, int l, int len8) {
        for (int i = 0; i < l; i++) {
            if (i < len8) {
                mp[i] = m[i];
            } else if (i == l - 1) {
                mp[i] = (byte) (len8 & 0x1F);
            } else {
                mp[i] = 0x00;
            }
        }
    }

    // Padding function: pads the byte length of the message mod 32 to the last incomplete block.
    // For complete blocks it returns the same block. For an empty block it returns a 0^2n string.
    // The function is called for full block messages to add a 0^2n block. This and the modulus are
    // the only differences compared to the use in Romulus-N 
    public static void ipad_128(byte[] m, byte[] mp, int l, int len8) {
        for (int i = 0; i < l; i++) {
            if (i < len8) {
                mp[i] = m[i];
            } else if (i == l - 1) {
                mp[i] = (byte) (len8 & 0xF);
            } else {
                mp[i] = 0x00;
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        // Example usage or testing
        byte[] input = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07}; // Replace with actual input data
        byte[] output = new byte[32]; // 256 bits, replace with actual output buffer
        crypto_hash(output, input, input.length);
        System.out.println("Hash:     " + bytesToHex(output));
        System.out.println("expected: " + "FA6CB425E9FB07E7643D429D4C0A0A77D134AD5F7B6E0BB7195FD1B3DB4C7E83");

    }


    public Romulus_H() throws NoSuchAlgorithmException {
        bitWidth = 256;
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(false);

        length = 0;
        virgin = true;
        initialize(h, g);
    }

    @Override
    public void reset() {
        length = 0;
        virgin = true;
        initialize(h, g);
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {

        if (lastMessageLength > 0) {
            // TODO:
            // take into account any remaining bytes from the last update call
            // let "in" be the lastMessage + newMessage
        }
        long mlen = length;
        byte[] in = null;

        // resolve offset
        if (offset == 0) {
            in = bytes;
        } else {
            in = Arrays.copyOfRange(bytes, offset, offset+length);
        }

        // work on full 32 byte message blocks
        while (mlen >= 32) {
            hirose_128_128_256(h, g, in);
            in = Arrays.copyOfRange(in, 32, in.length);
            mlen -= 32;
        }

        // save the remaining bytes for the doFinal()/digest() method
        lastMessageLength = (int)mlen; // at this stage mlen is <= 32
        if (lastMessageLength > 0) {
            System.arraycopy(in, 0, lastMessage, 0, lastMessageLength);
        }
    }

    private byte[] doFinalDigest() {
        byte[] out = new byte[32];
        byte[] p = new byte[32];
        ipad_256(lastMessage, p, 32, lastMessageLength);
        h[0] ^= 2;
        hirose_128_128_256(h, g, p);

        System.arraycopy(h, 0, out, 0, 16);
        System.arraycopy(g, 0, out, 16, 16);

        return out;
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = doFinalDigest();
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }
}