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

package net.jacksum.actions.io.verify;

import java.util.List;
import net.jacksum.cli.Verbose;

/**
 * The parameters for the CheckAction
 */
public interface CheckActionParameters {

    /**
     * Returns the name of the check file (the file that contains the hash values to be verified).
     *
     * @return the name of the check file
     */
    String getCheckFile();

    /**
     * Returns the verbosity settings.
     *
     * @return the verbosity settings
     */
    Verbose getVerbose();

    /**
     * Tells whether only a list of file names should be printed instead of the verification status.
     *
     * @return true if a list of file names is wanted (option -l)
     */
    boolean isList();
    
    /**
     * Returns the name of the character set that is used to read the check file.
     *
     * @return the name of the character set of the check file
     */
    String getCharsetCheckFile();
    
    /**
     * Returns the ID of the compatibility (predefined style) that is used to parse the check file.
     *
     * @return the compatibility ID, or null if none has been set
     */
    String getCompatibilityID();
    
    /**
     * Sets the file names that have been read from the check file.
     *
     * @param filenamesFromCheckFile the file names from the check file
     */
    void setFilenamesFromCheckFile(List<String> filenamesFromCheckFile);
    
    /**
     * Returns the file names that have been read from the check file.
     *
     * @return the file names from the check file, or null if they have not been set yet
     */
    List<String> getFilenamesFromCheckFile();
}
