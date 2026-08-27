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
 * Fugue2-256, the 256-bit member of the Fugue 2.0 family (April 2012), a tweak of the
 * original Fugue that needs fewer SMIX applications per input word.
 *
 * <p>Parameters: {@code F[n=8, s=30, k=1, r=26, t=13]}, digest length 256 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000100} (256); the value here is taken from section 4.4 of the
 * Fugue 2.0 specification.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue2_256 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 256;

    /** The fixed initial vector of Fugue2-256. */
    static final int[] IV = {
            0xe01b63da, 0xc48707e9, 0xe9a98eec, 0xa46b3915,
            0xa6de572c, 0x3f743cbe, 0x4105b317, 0x4580a1c6
    };

    /** Creates a Fugue2-256 message digest. */
    public Fugue2_256() {
        super("Fugue2-256", 8, 30, 1, 26, 13, IV, TIX_FUGUE2_30_1);
    }
}
