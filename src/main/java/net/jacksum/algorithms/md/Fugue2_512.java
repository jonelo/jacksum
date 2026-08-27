/*

  Jacksum 4.0.1 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

/**
 * Fugue2-512, the 512-bit member of the Fugue 2.0 family (April 2012), a tweak of the
 * original Fugue that needs fewer SMIX applications per input word.
 *
 * <p>Parameters: {@code F[n=16, s=36, k=3, r=14, t=13]}, digest length 512 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000200} (512); the value here is taken from section 5.3 of the
 * Fugue 2.0 specification.</p>
 *
 * <p>Note that section 5.3 of the specification names the one-word input as the byte sequence
 * {@code 00 00 02 80} while calling it "the decimal number 512". That is a typo:
 * {@code 0x00000280} is 640, and only {@code 0x00000200} reproduces the IV published there
 * (which {@code FugueSelfCheckTest} verifies). The original Fugue uses {@code 0x00000200} for
 * its 512-bit variant as well.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue2_512 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 512;

    /** The fixed initial vector of Fugue2-512. */
    static final int[] IV = {
            0x9010bba7, 0xa7a999bc, 0xe479d955, 0xd50e2474,
            0xc0d1b8c6, 0x3db445f3, 0x6b00cb8a, 0xb1057fc7,
            0xa2ef9305, 0x70c632f8, 0x9834386c, 0xac3c9940,
            0xb3c8ba4a, 0xecafa8e5, 0xea32fa93, 0x530a723b
    };

    /** Creates a Fugue2-512 message digest. */
    public Fugue2_512() {
        super("Fugue2-512", 16, 36, 3, 14, 13, IV, TIX_FUGUE2_36_3);
    }
}
