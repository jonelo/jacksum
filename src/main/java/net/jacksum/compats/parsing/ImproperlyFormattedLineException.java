/*

  Jacksum 3.4.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.compats.parsing;

/**
 *
 * @author Johann N. Loefflmann
 */
public class ImproperlyFormattedLineException extends Exception {

    /**
     * Creates a new instance of <code>ParseLineException</code> without detail
     * message.
     */
    public ImproperlyFormattedLineException() {
    }

    /**
     * Constructs an instance of <code>ParseLineException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ImproperlyFormattedLineException(String msg) {
        super(msg);
    }
}
