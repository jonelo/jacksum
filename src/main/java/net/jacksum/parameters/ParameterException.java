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

    /**
     * Constructs a {@code ParameterException} for an algorithm that cannot be used,
     * together with the hints that help to find a valid algorithm ID.
     *
     * <p>Every place that resolves the algorithm uses this, so that the same problem is
     * reported in the same way, no matter which action is being performed.</p>
     *
     * @param message the message of the algorithm lookup, e.g. why it has failed
     * @return the exception to be thrown
     */
    public static ParameterException forAlgorithm(String message) {
        return new ParameterException(message
                + "\nUse -a <code> to specify a valid algorithm ID."
                + "\nType \"jacksum -a all -l\" to list all supported algorithm IDs."
                + "\nType \"jacksum -a all:<string> -l\" to list all algorithms that contain a particular string."
                + "\nType \"jacksum -a all:<length> -l\" to list all algorithms that produce output of a particular bit length.");
    }
}
