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

package net.jacksum.actions.info.help;

/**
 * The parameters for the Help Action.
 */
public interface HelpActionParameters {
    /**
     * Tells whether a search string for the help has been set.
     *
     * @return true if a help search string has been set
     */
    boolean isHelpSearchString();

    /**
     * Gets the string to search for in the help.
     *
     * @return the help search string, or null if none has been set
     */
    String getHelpSearchString();

    /**
     * Tells whether a language for the help has been set.
     *
     * @return true if a help language has been set
     */
    boolean isHelpLanguage();

    /**
     * Gets the language code of the help (e.g. en).
     *
     * @return the help language code, or null if none has been set
     */
    String getHelpLanguage();

    /**
     * Tells whether the search string must match an option, an algorithm ID, or
     * a section header exactly rather than just being a prefix of it (option --exact).
     *
     * @return true if an exact match is required
     */
    boolean isExact();

}
