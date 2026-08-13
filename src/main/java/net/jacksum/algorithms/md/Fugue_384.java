/*

  Jacksum 4.0.0 - a checksum/hash tool written in Java
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
 * Fugue-384, the 384-bit member of the original Fugue family as submitted to round 2
 * of the NIST hash function competition (tweaked version, October 2009).
 *
 * <p>Parameters: {@code F[n=12, s=36, k=3, r=6, t=13]}, digest length 384 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000180} (384); the value here is taken from the step-by-step trace
 * {@code Supporting_Documentation/iv_debug.txt} of the submission.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue_384 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 384;

    /** The fixed initial vector of Fugue-384. */
    static final int[] IV = {
            0xaa61ec0d, 0x31252e1f, 0xa01db4c7, 0x00600985,
            0x215ef44a, 0x741b5e9c, 0xfa693e9a, 0x473eb040,
            0xe502ae8a, 0xa99c25e0, 0xbc95517c, 0x5c1095a1
    };

    /** Creates a Fugue-384 message digest. */
    public Fugue_384() {
        super("Fugue-384", 12, 36, 3, 6, 13, IV, TIX_FUGUE);
    }
}
