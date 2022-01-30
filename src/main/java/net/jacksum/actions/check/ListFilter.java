/*

  Jacksum 3.2.0 - a checksum utility in Java
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
package net.jacksum.actions.check;

/**
 *
 * @author Johann N. Loefflmann
 */
public class ListFilter {

    private boolean filterOk;
    private boolean filterFailed;
    private boolean filterNew;
    private boolean filterMissing;
    
    public ListFilter() {
        filterOk = true;
        filterFailed = true;
        filterNew = true;
        filterMissing = true;
    }

    public void enableAll(boolean bool) {
        filterOk = bool;
        filterFailed = bool;
        filterNew = bool;
        filterMissing = bool;
    }
    
    public boolean isHashingRequired() {
        return filterOk || filterFailed;
    }

    /**
     * @return the filterOk
     */
    public boolean isFilterOk() {
        return filterOk;
    }

    /**
     * @param filterOk the filterOk to set
     */
    public void setFilterOk(boolean filterOk) {
        this.filterOk = filterOk;
    }

    /**
     * @return the filterFailed
     */
    public boolean isFilterFailed() {
        return filterFailed;
    }

    /**
     * @param filterFailed the filterFailed to set
     */
    public void setFilterFailed(boolean filterFailed) {
        this.filterFailed = filterFailed;
    }

    /**
     * @return the filterNew
     */
    public boolean isFilterNew() {
        return filterNew;
    }

    /**
     * @param filterNew the filterNew to set
     */
    public void setFilterNew(boolean filterNew) {
        this.filterNew = filterNew;
    }

    /**
     * @return the FilterMissing
     */
    public boolean isFilterMissing() {
        return filterMissing;
    }

    /**
     * @param filterMissing the filterMissing to set
     */
    public void setFilterMissing(boolean filterMissing) {
        this.filterMissing = filterMissing;
    }
    
    

    public void setFilter(String arg) throws IllegalArgumentException {
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
                case "bad":
                   filterFailed = true;
                   filterMissing = true;
                   filterOk = false;
                   filterNew = false;
                    break;
                case "good":
                   filterFailed = false;
                   filterMissing = false;
                   filterOk = true;
                   filterNew = true;
                    break;
                case "ok":
                    setFilterOk(true);
                    break;
                case "failed":
                    setFilterFailed(true);
                    break;
                case "new":
                    setFilterNew(true);
                    break;
                case "missing":
                    setFilterMissing(true);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("%s is an invalid parameter", token));
            }
        }
    }
}
