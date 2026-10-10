/*
 * Jacksum 4.0.2 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

package net.jacksum.parameters.base;

import java.nio.charset.Charset;

/**
 * Parameters for reading a list of strings from a file (option --string-list).
 */
public interface StringListParameters {
    /**
     * Returns the name of the file that contains the strings.
     *
     * @return the file name, or null if not set
     */
    public String getStringList();
    /**
     * Tells whether a string list has been set.
     *
     * @return true if a string list has been set
     */
    public boolean isStringList();
    /**
     * Sets the name of the file that contains the strings.
     *
     * @param list the file name
     */
    public void setStringList(String list);
    /**
     * Returns the charset of the string list (option --charset-string-list).
     *
     * @return the name of the charset, or null if not set
     */
    public String getCharsetStringList();
    /**
     * Returns the characters that mark comment lines to be ignored (option -I).
     *
     * @return the characters that mark comment lines, or null if not set
     */
    public String getCommentChars();
    /**
     * Tells whether empty lines should be ignored (option --ignore-empty-lines).
     *
     * @return true if empty lines should be ignored
     */
    public boolean isIgnoreEmptyLines();
}
