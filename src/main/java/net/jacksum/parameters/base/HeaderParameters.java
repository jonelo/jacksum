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
 * The parameters that control the header of the output.
 */
public interface HeaderParameters {
    /**
     * Returns the line separator.
     * @return the line separator
     */
    String getLineSeparator();
    /**
     * Returns the characters that start a comment line.
     * @return the comment characters
     */
    String getCommentChars();
    /**
     * Returns the command line arguments.
     * @return the command line arguments
     */
    String[] getCLIParameters();
    /**
     * Returns the command line arguments, quoted where necessary so that they
     * can be written to the header.
     * @return the quoted command line arguments
     */
    String[] getCLIParametersWithQuotes();
    /**
     * Determines whether a header should be written.
     * @return true if a header should be written
     */
    boolean isHeaderWanted();
    /**
     * Returns the header line that is written before the regular header.
     * @return the leading header
     */
    String getLeadingHeader();
}
