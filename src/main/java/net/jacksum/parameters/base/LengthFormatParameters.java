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
package net.jacksum.parameters.base;

/**
 * The parameters that control how the file size is formatted.
 *
 * @author Johann
 */
public interface LengthFormatParameters {

    /**
     * Returns the size of the blocks in which the file size is expressed.
     * @return the block size in bytes
     */
    long getFilesizeAsByteBlocks();

    /**
     * Returns the printf format for the file size.
     * @return the printf format for the file size
     */
    String getFilesizeWithPrintfFormatted();

    /**
     * Determines whether it has been set whether the file size is wanted.
     * @return true if it has been set whether the file size is wanted
     */
    boolean isFilesizeWantedSet();

    /**
     * Determines whether the file size is wanted in the output.
     * @return true if the file size is wanted
     */
    boolean isFilesizeWanted();

}
