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
 * Fugue2-224, the 224-bit member of the Fugue 2.0 family (April 2012), a tweak of the
 * original Fugue that needs fewer SMIX applications per input word.
 *
 * <p>Parameters: {@code F[n=7, s=30, k=1, r=15, t=13]}, digest length 224 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x000000e0} (224); the value here is taken from section 5.3 of the
 * Fugue 2.0 specification.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue2_224 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 224;

    /** The fixed initial vector of Fugue2-224. */
    static final int[] IV = {
            0x3a1d28af, 0xdb9b0b75, 0x66673079, 0xae45c71c,
            0x7efbd0e1, 0xad70e5be, 0x85430488
    };

    /** Creates a Fugue2-224 message digest. */
    public Fugue2_224() {
        super("Fugue2-224", 7, 30, 1, 15, 13, IV, TIX_FUGUE2_30_1);
    }
}
