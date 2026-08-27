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
package net.jacksum.algorithms;

import java.util.Locale;

/**
 * The state that tells whether a cryptographic algorithm is considered broken.
 * <p>
 * The states are derived from the field "broken:" of the algorithm's
 * documentation in Jacksum's help file, see {@link BrokenStateRegistry}.
 *
 * @since 4.0.0
 */
public enum BrokenState {

    /**
     * The algorithm is not known to be broken, i.e. no attack that is
     * significantly better than the generic attack is known.
     */
    NO("no"),

    /**
     * At least one, but not all of the algorithm's security properties are
     * broken (e.g. the preimage resistance is broken, while the collision
     * resistance is not).
     */
    PARTLY("partly"),

    /**
     * The algorithm is broken.
     */
    YES("yes"),

    /**
     * Whether the algorithm is broken depends on a parameter of the algorithm,
     * e.g. on the underlying hash function of an HMAC.
     */
    DEPENDS("depends"),

    /**
     * The question whether the algorithm is broken does not apply, because the
     * algorithm does not claim any cryptographic security properties (e.g. a
     * CRC or a checksum), or because the algorithm is unknown to Jacksum's
     * documentation.
     */
    NOT_APPLICABLE("n/a");

    private final String token;

    BrokenState(String token) {
        this.token = token;
    }

    /**
     * Returns the token of this state as it is used in Jacksum's help file and
     * in the output of the option --info.
     *
     * @return the token of this state, e.g. "yes"
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns whether at least one security property of the full algorithm is
     * broken, i.e. whether this state is {@link #YES} or {@link #PARTLY}.
     * <p>
     * Note that {@link #DEPENDS} and {@link #NOT_APPLICABLE} return false,
     * because no statement about the algorithm itself can be made in those
     * cases. Use {@link #getToken()} or a comparison with the enum constants if
     * you need to distinguish all cases.
     *
     * @return true if this state is {@link #YES} or {@link #PARTLY}
     */
    public boolean isBroken() {
        return this == YES || this == PARTLY;
    }

    /**
     * Returns the token of this state.
     *
     * @return the token of this state, e.g. "yes"
     */
    @Override
    public String toString() {
        return token;
    }

    /**
     * Maps the first word of the value of the field "broken:" in Jacksum's help
     * file to a BrokenState. Trailing punctuation is ignored, so both "no;" and
     * "no," map to {@link #NO}.
     *
     * @param word the first word of the value of the field "broken:", can be
     * null
     * @return the corresponding BrokenState, {@link #NOT_APPLICABLE} if the
     * word cannot be mapped
     */
    public static BrokenState fromHelpToken(String word) {
        if (word == null) {
            return NOT_APPLICABLE;
        }
        String normalized = word.toLowerCase(Locale.US);
        while (!normalized.isEmpty()) {
            char last = normalized.charAt(normalized.length() - 1);
            if (last == ';' || last == ',' || last == '.' || last == ':') {
                normalized = normalized.substring(0, normalized.length() - 1);
            } else {
                break;
            }
        }
        for (BrokenState state : values()) {
            if (state.token.equals(normalized)) {
                return state;
            }
        }
        return NOT_APPLICABLE;
    }

}
