/*

  Jacksum 3.4.0 - a checksum utility in Java
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
package net.jacksum.actions.io.wanted;

import net.loefflmann.sugar.util.Transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Johann N. Loefflmann
 */
public class MatchFilter implements Serializable  {

    private static final long serialVersionUID = 8389892691520112065L;
    private boolean filterMatch;
    private boolean filterNoMatch;

    public MatchFilter() {
        filterMatch = true;
        filterNoMatch = false;
    }

    public void enableAll(boolean bool) {
        filterMatch = bool;
        filterNoMatch = bool;
    }
    
    public boolean isHashingRequired() {
        return true;
    }


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

    private boolean filterHasBeenSet = false;

    public boolean isFilterHasBeenSet() {
        return filterHasBeenSet;
    }

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
                    throw new IllegalArgumentException(String.format("%s is an invalid parameter", token));
            }
        }
    }

    public boolean isFilterMatch() {
        return filterMatch;
    }

    public void setFilterMatch(boolean filterMatch) {
        this.filterMatch = filterMatch;
    }

    public boolean isFilterNoMatch() {
        return filterNoMatch;
    }

    public void setFilterNoMatch(boolean filterNoMatch) {
        this.filterNoMatch = filterNoMatch;
    }
}
