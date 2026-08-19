/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
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

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author Johann N. Loefflmann
 */
public class MessagePayload {


    private Path path;
    private String specialPath; // for keeping filenames that a Path object oes not support such as "\\.\c:\" or ADS paths on Windows
    private byte[] digest;
    private long size;
    private BasicFileAttributes basicFileAttributes;
    private boolean fileNotFound;

    public void setPath(Path path) {
        this.path = path;
    }

    public void setSpecialPath(String specialPath) {
        this.specialPath = specialPath;
    }

    public Path getPath() {
        return path;
    }

    public String getSpecialPath() {
        return specialPath;
    }

    /**
     * Determines whether the error that this payload belongs to is that the file cannot be found
     * at all. It allows a verification (option -c) to tell a file that is missing apart from a
     * file that exists, but that could not be processed for a different reason.
     *
     * @return true if the file cannot be found
     */
    public boolean isFileNotFound() {
        return fileNotFound;
    }

    /**
     * @param fileNotFound whether the file cannot be found at all
     */
    public void setFileNotFound(boolean fileNotFound) {
        this.fileNotFound = fileNotFound;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public byte[] getDigest() {
        return digest;
    }


    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }
    
    /**
     * @return the basicFileAttributes
     */
    public BasicFileAttributes getBasicFileAttributes() {
        return basicFileAttributes;
    }

    /**
     * @param basicFileAttributes the basicFileAttributes to set
     */
    public void setBasicFileAttributes(BasicFileAttributes basicFileAttributes) {
        this.basicFileAttributes = basicFileAttributes;
    }

}
