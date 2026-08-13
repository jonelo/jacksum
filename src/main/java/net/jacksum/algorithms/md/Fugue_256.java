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
 * Fugue-256, the 256-bit member of the original Fugue family as submitted to round 2
 * of the NIST hash function competition (tweaked version, October 2009).
 *
 * <p>Parameters: {@code F[n=8, s=30, k=2, r=5, t=13]}, digest length 256 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000100} (256); the value here is taken from the step-by-step trace
 * {@code Supporting_Documentation/iv_debug.txt} of the submission.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue_256 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 256;

    /** The fixed initial vector of Fugue-256. */
    static final int[] IV = {
            0xe952bdde, 0x6671135f, 0xe0d4f668, 0xd2b0b594,
            0xf96c621d, 0xfbf929de, 0x9149e899, 0x34f8c248
    };

    /** Creates a Fugue-256 message digest. */
    public Fugue_256() {
        super("Fugue-256", 8, 30, 2, 5, 13, IV, TIX_FUGUE);
    }
}
