/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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
package net.jacksum.actions.io.compare;

import java.util.Arrays;
import net.jacksum.actions.Action;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.cli.ExitCode;

/**
 * Base class for actions that compare a calculated hash value with an
 * expected one and count the matches and mismatches.
 */
public abstract class CompareAction implements Action {

    /** The number of comparisons that resulted in a match. */
    protected int positives = 0;
    /** The number of comparisons that resulted in a mismatch. */
    protected int negatives = 0;

    /** The checksum that provides the calculated hash value. */
    protected AbstractChecksum checksum;
    /** The parameters that provide the expected hash value. */
    protected CompareActionInterface parameters;

    /**
     * Creates a new CompareAction.
     */
    public CompareAction() {
    }

    // Tells us whether the expected string is equals to the checksum value (be tolerant).
    // Tolerant means equalsIngoreCase except if we have an encoding with a case-sensitive
    // alphabet (e.g. BASE64 and Z85), see also Encoding.hashesAreEqual()
    /**
     * Determines whether the formatted value of the checksum equals the expected
     * string. The comparison ignores case, unless the encoding uses a
     * case-sensitive alphabet (e.g. Base64 or Z85).
     *
     * @param checksum the checksum that provides the formatted value
     * @param expected the expected hash value as a string
     * @return true if both values are considered equal
     */
    protected boolean equalsTolerant(AbstractChecksum checksum, String expected) {
        String value = checksum.getValueFormatted();
        //String value = checksum.getformatter.getFingerprintFormatter().format(getByteArray());
        return Encoding.hashesAreEqual(value, expected, checksum.getFormatPreferences().getEncoding());
    }

    /**
     * Returns the number of comparisons that resulted in a match.
     *
     * @return the number of matches
     */
    public int getPositives() {
        return positives;
    }

    /**
     * Returns the number of comparisons that resulted in a mismatch.
     *
     * @return the number of mismatches
     */
    public int getNegatives() {
        return negatives;
    }

    /**
     * Processes the result of a single comparison.
     *
     * @param b true if the calculated value matches the expected value
     */
    abstract public void perform(boolean b);

    @Override
    public int perform() {
        try {
            perform(Arrays.equals(
                    checksum.getByteArray(),
                    parameters.getExpectedBytes()));
        } catch (UnsupportedOperationException e) {
            perform(equalsTolerant(
                    checksum,
                    parameters.getExpectedString()));
        }
        return (getPositives() > 0) ? ExitCode.OK : ExitCode.CHECK_MISMATCH;
    }

}
