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
package net.jacksum.multicore.manyfiles;

import net.jacksum.actions.io.verify.ListFilter;
import net.jacksum.parameters.base.StdinParameters;

import java.nio.file.Path;
import java.util.List;

/**
 * The parameters that are required by the producer that determines the files to be processed.
 * @author Johann
 */
public interface ProducerParameters extends StdinParameters {
    /**
     * Returns the file names that have been specified on the command line.
     *
     * @return the file names from the command line arguments
     */
    List<String> getFilenamesFromArgs();
    /**
     * Returns the file names that have been read from a file list (--file-list).
     *
     * @return the file names from the file list
     */
    List<String> getFilenamesFromFilelist();
    /**
     * Returns the file names that have been read from the check file.
     *
     * @return the file names from the check file, or null if no check file has been read
     */
    List<String> getFilenamesFromCheckFile();
    /**
     * Tells whether standard input has been specified among the file names on the command line.
     *
     * @return true if standard input should be read
     */
    boolean isStdinForFilenamesFromArgs();
    /**
     * Returns the maximum depth for recursion (-r).
     *
     * @return the maximum depth for recursion
     */
    int getDepth();
    /**
     * Tells whether symbolic links to files should not be followed (-f).
     *
     * @return true if symbolic links to files should not be followed
     */
    boolean isDontFollowSymlinksToFiles();
    /**
     * Tells whether symbolic links to directories should not be followed.
     *
     * @return true if symbolic links to directories should not be followed
     */
    boolean isDontFollowSymlinksToDirectories();
    /**
     * Returns the filter that determines which files are listed during a check.
     *
     * @return the list filter
     */
    ListFilter getListFilter();
    /**
     * Tells whether an output file has been specified.
     *
     * @return true if an output file has been specified
     */
    boolean isOutputFile();
    /**
     * Returns the name of the output file.
     *
     * @return the name of the output file
     */
    String getOutputFile();
    /**
     * Tells whether an error file has been specified.
     *
     * @return true if an error file has been specified
     */
    boolean isErrorFile();
    /**
     * Returns the name of the error file.
     *
     * @return the name of the error file
     */
    String getErrorFile();
    /**
     * Tells whether all Unix file types should be scanned (--scan-all-unix-file-types).
     *
     * @return true if all Unix file types should be scanned
     */
    boolean scanAllUnixFileTypes();
    /**
     * Tells whether NTFS alternate data streams should be scanned (--scan-ntfs-ads).
     *
     * @return true if NTFS alternate data streams should be scanned
     */
    boolean isScanNtfsAds();
    /**
     * Tells whether absolute paths should be used (--path-absolute).
     *
     * @return true if absolute paths should be used
     */
    boolean isPathAbsolute();
    /**
     * Returns the path that output paths are relative to (--path-relative-to).
     *
     * @return the path that output paths are relative to, or null if it has not been set
     */
    Path getPathRelativeTo();
}
