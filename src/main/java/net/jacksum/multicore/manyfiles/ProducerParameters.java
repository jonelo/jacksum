/*


  Jacksum 3.6.0 - a checksum utility in Java
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
package net.jacksum.multicore.manyfiles;

import net.jacksum.actions.io.verify.ListFilter;

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Johann
 */
public interface ProducerParameters {
    List<String> getFilenamesFromArgs();
    List<String> getFilenamesFromFilelist();
    List<String> getFilenamesFromCheckFile();
    boolean isStdinForFilenamesFromArgs();
    int getDepth();
    boolean isDontFollowSymlinksToFiles();
    boolean isDontFollowSymlinksToDirectories();
    String getStdinName();
    ListFilter getListFilter();
    boolean isOutputFile();
    String getOutputFile();
    boolean isErrorFile();
    String getErrorFile();
    boolean scanAllUnixFileTypes();
    boolean isScanNtfsAds();
    boolean isPathAbsolute();
    Path getPathRelativeTo();
}
