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
package net.jacksum.actions.hashfiles;

import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;
import net.loefflmann.sugar.math.GeneralMath;


public class MessageConsumerStatisticsForHashedFiles extends Statistics {

    private long filesRead;
    private long errors;
    private long bytesRead;
    private long filesMatchesExpectation = -1;
    private long filesMatchesWanted = -1;
    private long filesNoMatchesWanted = -1;
   
    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();        
        map.put("files read successfully", filesRead);
        map.put("files read with errors", errors);
        map.put("total bytes read", bytesRead);
        map.put("total bytes read (human readable)", GeneralMath.formatByteCountHumanReadable(bytesRead, true));
        if (filesMatchesExpectation > -1) {
            map.put("files matching expected hash", filesMatchesExpectation);
        }
        if (filesMatchesWanted > -1) {
            map.put("files matching wanted hashes", filesMatchesWanted);
        }
        if (filesNoMatchesWanted > -1) {
            map.put("files not matching wanted hashes", filesNoMatchesWanted);
        }
        return map;
    }

    @Override
    public void reset() {
        filesRead = 0;
        errors = 0;
        bytesRead = 0;
        filesMatchesExpectation = -1;
        setFilesMatchesWanted(-1);
    }

    /**
     * @return the filesRead
     */
    public long getFilesRead() {
        return filesRead;
    }

    /**
     * @param filesRead the filesRead to set
     */
    public void setFilesRead(long filesRead) {
        this.filesRead = filesRead;
    }

    /**
     * @return the errors
     */
    public long getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(long errors) {
        this.errors = errors;
    }

    /**
     * @return the bytesRead
     */
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * @param bytesRead the bytesRead to set
     */
    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
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
}
