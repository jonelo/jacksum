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
 * Fugue2-384, the 384-bit member of the Fugue 2.0 family (April 2012), a tweak of the
 * original Fugue that needs fewer SMIX applications per input word.
 *
 * <p>Parameters: {@code F[n=12, s=36, k=2, r=14, t=13]}, digest length 384 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000180} (384); the value here is taken from section 5.3 of the
 * Fugue 2.0 specification.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue2_384 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 384;

    /** The fixed initial vector of Fugue2-384. */
    static final int[] IV = {
            0x8e4f1231, 0x837e3d2a, 0xec427f83, 0x925ac741,
            0x69fadae5, 0x15398593, 0x657d34f8, 0x667eef64,
            0x3d06ff8b, 0x1440123a, 0x2d5101be, 0x9d119f61
    };

    /** Creates a Fugue2-384 message digest. */
    public Fugue2_384() {
        super("Fugue2-384", 12, 36, 2, 14, 13, IV, TIX_FUGUE2_36_2);
    }
}
