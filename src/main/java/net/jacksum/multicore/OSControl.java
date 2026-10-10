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

package net.jacksum.multicore;

import java.util.Locale;

/**
 * Determines the operating system Jacksum is running on.
 */
public class OSControl {
    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.US);

    /**
     * Creates a new OSControl.
     */
    public OSControl() {
    }

    /**
     * Determines whether the operating system is Microsoft Windows.
     *
     * @return true if running on Windows
     */
    public static final boolean isWindows() {
        return OS.startsWith("windows");
    }

    /**
     * Determines whether the operating system is macOS.
     *
     * @return true if running on macOS
     */
    public static final boolean isMacOS() {
        return OS.startsWith("mac os");
    }

}
