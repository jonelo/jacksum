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

package net.jacksum.actions;

/**
 * All action types that are supported by Jacksum.
 */
public enum ActionType {
    // info actions
    /** Prints the list of algorithms that can be used for HMAC. */
    HMACS,
    /** Prints the help (the manpage). */
    HELP,
    /** Prints information about the application. */
    INFO_APP,
    /** Prints information about an algorithm. */
    INFO_ALGO,
    /** Prints information about a compatibility (predefined style). */
    INFO_COMPAT,
    /** Prints the copyright. */
    COPYRIGHT,
    /** Prints the license. */
    LICENSE,
    /** Prints the version. */
    VERSION,
    /** Calculates the hash of a sequence (quick mode). */
    QUICK,
    // io actions
    /** Calculates the hashes of files. */
    HASH_FILES,
    /** Calculates the hashes of files and finds files that match wanted hashes. */
    WANTED_LIST,
    /** Checks the integrity of files against a check file or check line. */
    CHECK,
    /** Finds the algorithm(s) that produced a given hash value. */
    FIND_ALGO,
    /** Calculates the hashes of the strings in a string list. */
    STRING_LIST,
}
