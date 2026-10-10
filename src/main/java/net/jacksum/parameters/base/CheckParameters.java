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

import net.jacksum.actions.io.verify.ListFilter;

/**
 * Parameters for checking (verifying) files against a check file.
 */
public interface CheckParameters {

    /**
     * Returns the filter that controls which statuses are reported.
     *
     * @return the list filter
     */
    ListFilter getListFilter();
    /**
     * Determines whether the list view is enabled (option --list), that is
     * only file names are printed, without statuses.
     *
     * @return true if the list view is enabled
     */
    boolean isList();
    /**
     * Determines whether a strict check is performed (option --check-strict).
     *
     * @return true if a strict check is performed
     */
    boolean isCheckStrict();
    /**
     * Determines whether timestamps in the check file are ignored
     * (option --ignore-timestamps).
     *
     * @return true if timestamps are ignored
     */
    boolean isIgnoreTimestamps();
    /**
     * Determines whether hash values in the check file are ignored
     * (option --ignore-hashes).
     *
     * @return true if hash values are ignored
     */
    boolean isIgnoreHashes();
    /**
     * Determines whether file sizes in the check file are ignored
     * (option --ignore-sizes).
     *
     * @return true if file sizes are ignored
     */
    boolean isIgnoreSizes();
}
