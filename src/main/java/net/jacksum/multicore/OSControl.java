/*

  Jacksum 3.7.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

public class OSControl {
    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.US);

    public static final boolean isWindows() {
        return OS.startsWith("windows");
    }

    public static final boolean isMacOS() {
        return OS.startsWith("mac os");
    }

}
