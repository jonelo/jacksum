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
package net.jacksum.formats;

import net.jacksum.parameters.base.LengthFormatParameters;
import net.loefflmann.sugar.util.GeneralString;

public class SizeFormatter implements LengthFormatParameters {

    private final LengthFormatParameters parameters;

    public SizeFormatter(LengthFormatParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Converts a length in bytes into the unit that a format stores it in. Some algorithms store
     * the size of a file as a number of blocks rather than as a number of bytes, e.g. sum_bsd,
     * sum_sysv, and sum_minix, which follow the output of the BSD resp. System V "sum" command.
     * A verification has to compare the size in the very same unit, see also
     * MessageConsumerOnCheckedFiles.
     *
     * @param length the length in bytes
     * @param filesizeAsByteBlocks the number of bytes that one block consists of, or -1 if the
     * size is stored in bytes
     * @return the length in the unit that the format stores it in
     */
    public static long lengthInUnitOfFormat(long length, long filesizeAsByteBlocks) {
        return filesizeAsByteBlocks == -1
                ? length
                : (length + (filesizeAsByteBlocks - 1)) / filesizeAsByteBlocks;
    }

    public String format(long length) {
        long output = lengthInUnitOfFormat(length, parameters.getFilesizeAsByteBlocks());

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


    @Override
    public boolean isFilesizeWantedSet() {
        return parameters.isFilesizeWantedSet();
    }

    @Override
    public boolean isFilesizeWanted() {
        return parameters.isFilesizeWanted();
    }

}
