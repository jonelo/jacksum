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
package net.jacksum.parameters.base;

/**
 * The number of threads that a run is allowed to use.
 */
public interface ThreadParameters {

    /**
     * Gets the number of threads that are used to calculate hashes.
     *
     * @return the number of threads for hashing
     */
    int getThreadsHashing();

    /**
     * Gets the number of threads that are used to read files.
     *
     * @return the number of threads for reading
     */
    int getThreadsReading();

}
