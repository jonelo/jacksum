/*


  Jacksum 3.5.0 - a checksum utility in Java
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
package net.jacksum.statistics;

import java.util.LinkedHashMap;
import java.util.Map;


public class StatisticsForHashedFiles extends CommonHashStatistics {

    private long filesMatchesExpectation = -1;
    private long totalNumberOfWantedHashes = -1;
    private long filesMatchesWanted = -1;
    private long filesNoMatchesWanted = -1;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        if (filesMatchesExpectation > -1) {
            map.put("files matching expected hash", filesMatchesExpectation);
        }
        if (totalNumberOfWantedHashes > -1) {
            map.put("total number of wanted hashes", totalNumberOfWantedHashes);
        }
        if (filesMatchesWanted > -1) {
            map.put("files matching wanted hashes (MATCH)", filesMatchesWanted);
        }
        if (filesNoMatchesWanted > -1) {
            map.put("files not matching wanted hashes (NO MATCH)", filesNoMatchesWanted);
        }
        super.put(map);
        return map;
    }

    @Override
    public void reset() {
        super.reset();
        filesMatchesExpectation = -1;
        totalNumberOfWantedHashes = -1;
        filesMatchesWanted = -1;
        filesNoMatchesWanted = -1;
    }

    /**
     * @return the filesMatchesExpectation
     */
    public long getFilesMatchesExpectation() {
        return filesMatchesExpectation;
    }

    /**
     * @param filesMatchesExpectation the filesMatchesExpectation to set
     */
    public void setFilesMatchesExpectation(long filesMatchesExpectation) {
        this.filesMatchesExpectation = filesMatchesExpectation;
    }


    /**
     * Returns the number of files that match wanted hashes
     * @return the number of files that match wanted hashes
     */
    public long getFilesMatchesWanted() {
        return filesMatchesWanted;
    }

    /**
     * Sets the number of files that match wanted hashes
     * @param filesMatchesWanted the number of files that match wanted hashes
     */
    public void setFilesMatchesWanted(long filesMatchesWanted) {
        this.filesMatchesWanted = filesMatchesWanted;
    }

    public long getFilesNoMatchesWanted() {
        return filesNoMatchesWanted;
    }

    public void setFilesNoMatchesWanted(long filesNoMatchesWanted) {
        this.filesNoMatchesWanted = filesNoMatchesWanted;
    }

    public long getTotalNumberOfWantedHashes() {
        return totalNumberOfWantedHashes;
    }

    public void setTotalNumberOfWantedHashes(long totalNumberOfWantedHashes) {
        this.totalNumberOfWantedHashes = totalNumberOfWantedHashes;
    }

}
