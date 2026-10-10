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
package net.jacksum.actions.io.wanted;

import net.loefflmann.sugar.util.Transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter that controls which results are reported when files are compared
 * against a list of wanted hashes: files whose hash matches, files whose hash
 * does not match, both, or none.
 *
 * @author Johann N. Loefflmann
 */
public class MatchFilter implements Serializable  {

    private static final long serialVersionUID = -8775542673951649406L;

    /** Whether files with a matching hash should be reported. */
    private boolean filterMatch;
    /** Whether files without a matching hash should be reported. */
    private boolean filterNoMatch;

    /**
     * Creates a new {@code MatchFilter} that reports matches only.
     */
    public MatchFilter() {
        filterMatch = true;
        filterNoMatch = false;
    }

    /**
     * Enables or disables both the match and the no-match filter.
     *
     * @param bool true to report all files, false to report none
     */
    public void enableAll(boolean bool) {
        filterMatch = bool;
        filterNoMatch = bool;
    }
    
    /**
     * Tells whether files need to be hashed for this filter; always true.
     *
     * @return true
     */
    public boolean isHashingRequired() {
        return true;
    }


    /**
     * Returns the filter settings as a string, e.g. {@code all}, {@code none},
     * {@code positive}, or {@code negative}.
     *
     * @return the string representation of the filter settings
     */
    public String toString() {
        if (filterMatch && filterNoMatch) {
            return "all";
        }
        if (!filterMatch && !filterNoMatch) {
            return "none";
        }
        if (filterMatch && !filterNoMatch) {
            return "positive";
        }
        if (!filterMatch && filterNoMatch) {
            return "negative";
        }
        List<String> list = new ArrayList<>();
        if (filterMatch) {
            list.add("match");
        }
        if (filterNoMatch) {
            list.add("nomatch");
        }
        return Transformer.list2CsvString(list);
    }

    /** Whether the filter has been set explicitly by {@link #setFilter(String)}. */
    private boolean filterHasBeenSet = false;

    /**
     * Tells whether the filter has been set explicitly by {@link #setFilter(String)}.
     *
     * @return true if the filter has been set explicitly
     */
    public boolean isFilterHasBeenSet() {
        return filterHasBeenSet;
    }

    /**
     * Sets the filter from a comma separated list of the tokens {@code all},
     * {@code none}, {@code match}, {@code nomatch}, {@code positive}
     * (or {@code default}), and {@code negative}. All filters are disabled
     * before the tokens are applied from left to right.
     *
     * @param arg the comma separated list of filter tokens
     * @throws IllegalArgumentException if a token is invalid
     */
    public void setFilter(String arg) throws IllegalArgumentException {
        filterHasBeenSet = true;
        enableAll(false);
        String[] tokens = arg.split(",");
        for (String token : tokens) {
            switch (token.trim()) {
                case "all":
                    enableAll(true);
                    break;
                case "none":
                    enableAll(false);
                    break;
                case "match":
                    filterMatch = true;
                    break;
                case "default":
                case "positive":
                    filterMatch = true;
                    filterNoMatch = false;
                    break;
                case "nomatch":
                    filterNoMatch = true;
                    break;
                case "negative":
                    filterMatch = false;
                    filterNoMatch = true;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("%s is an invalid parameter.", token));
            }
        }
    }

    /**
     * Tells whether files with a matching hash should be reported.
     *
     * @return true if matching files should be reported
     */
    public boolean isFilterMatch() {
        return filterMatch;
    }

    /**
     * Sets whether files with a matching hash should be reported.
     *
     * @param filterMatch true if matching files should be reported
     */
    public void setFilterMatch(boolean filterMatch) {
        this.filterMatch = filterMatch;
    }

    /**
     * Tells whether files without a matching hash should be reported.
     *
     * @return true if non-matching files should be reported
     */
    public boolean isFilterNoMatch() {
        return filterNoMatch;
    }

    /**
     * Sets whether files without a matching hash should be reported.
     *
     * @param filterNoMatch true if non-matching files should be reported
     */
    public void setFilterNoMatch(boolean filterNoMatch) {
        this.filterNoMatch = filterNoMatch;
    }
}
