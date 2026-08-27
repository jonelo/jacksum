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
 * Fugue-512, the 512-bit member of the original Fugue family as submitted to round 2
 * of the NIST hash function competition (tweaked version, October 2009).
 *
 * <p>Parameters: {@code F[n=16, s=36, k=4, r=8, t=13]}, digest length 512 bits.
 * The initial vector is the result of applying the algorithm itself, with an all-zero IV, to
 * the single input word {@code 0x00000200} (512); the value here is taken from the step-by-step trace
 * {@code Supporting_Documentation/iv_debug.txt} of the submission.</p>
 *
 * @see Fugue_Mother
 */
public final class Fugue_512 extends Fugue_Mother {

    /** Digest length in bits. */
    public static final int DIGEST_BITS = 512;

    /** The fixed initial vector of Fugue-512. */
    static final int[] IV = {
            0x8807a57e, 0xe616af75, 0xc5d3e4db, 0xac9ab027,
            0xd915f117, 0xb6eecc54, 0x06e8020b, 0x4a92efd1,
            0xaac6e2c9, 0xddb21398, 0xcae65838, 0x437f203f,
            0x25ea78e7, 0x951fddd6, 0xda6ed11d, 0xe13e3567
    };

    /** Creates a Fugue-512 message digest. */
    public Fugue_512() {
        super("Fugue-512", 16, 36, 4, 8, 13, IV, TIX_FUGUE);
    }
}
