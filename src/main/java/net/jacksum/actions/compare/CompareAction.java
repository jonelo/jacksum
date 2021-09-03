/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.actions.compare;

import java.util.Arrays;
import net.jacksum.actions.Action;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.cli.ExitCode;

public abstract class CompareAction implements Action {

    protected int positives = 0;
    protected int negatives = 0;

    protected AbstractChecksum checksum;
    protected CompareActionInterface parameters;

    // Tells us whether the expected string is equals to the checksum value (be tolerant).
    // Tolerant means equalsIngoreCase except if we have BASE64 encoding,
    // because BASE64 encoding is case sensitive
    protected boolean equalsTolerant(AbstractChecksum checksum, String expected) {
        String value = checksum.getValueFormatted();
        //String value = checksum.getformatter.getFingerprintFormatter().format(getByteArray());
        if (checksum.getFormatPreferences().getEncoding().equals(Encoding.BASE64)) {
            // strict checking required for BASE64
            return value.equals(expected);
        } else {
            return value.equalsIgnoreCase(expected);
        }
    }

    public int getPositives() {
        return positives;
    }

    public int getNegatives() {
        return negatives;
    }

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
