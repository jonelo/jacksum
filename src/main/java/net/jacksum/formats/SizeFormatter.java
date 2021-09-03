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
package net.jacksum.formats;

import net.jacksum.parameters.base.LengthFormatParameters;
import org.n16n.sugar.util.GeneralString;

public class SizeFormatter implements LengthFormatParameters {

    private final LengthFormatParameters parameters;

    public SizeFormatter(LengthFormatParameters parameters) {
        this.parameters = parameters;
    }

    public String format(long length) {
        long output=0;

        if (parameters.getFilesizeAsByteBlocks() != -1) {
            output = (length + (parameters.getFilesizeAsByteBlocks() - 1)) / parameters.getFilesizeAsByteBlocks();
        } else {
            output = length;
        }
        if (parameters.getFilesizeWithPrintfFormatted() != null) {
            return String.format(parameters.getFilesizeWithPrintfFormatted(), output); // e.g. "%5s"
        }
        return Long.toString(output);
    }

    public static void replaceAliases(StringBuilder format) {
        GeneralString.replaceAllStrings(format, "#LENGTH", "#FILESIZE");
    }

    @Override
    public long getFilesizeAsByteBlocks() {
        return parameters.getFilesizeAsByteBlocks();
    }

    @Override
    public String getFilesizeWithPrintfFormatted() {
        return parameters.getFilesizeWithPrintfFormatted();
    }

}
