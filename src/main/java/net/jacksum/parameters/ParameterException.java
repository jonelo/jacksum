/*


  Jacksum 3.3.0 - a checksum utility in Java
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

package net.jacksum.parameters;

public class ParameterException extends Exception {

    /**
     * Constructs a {@code ParameterException} with
     * {@code null} as its error detail message.
     */
    public ParameterException() {
        super();
    }

    /**
     * Constructs a {@code ParameterException}
     * with the specified detail message.
     *
     * @param message the detail message.
     */
    public ParameterException(String message) {
        super("Jacksum: Parameter Error: "+message);
    }
}
