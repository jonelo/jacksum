/** 
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 *
 *******************************************************************************
 */

package net.jacksum.multicore.manyfiles;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author johann
 */
public class MessagePayload {


    private Path path;
    private byte[] digest;
    private long size;
    private BasicFileAttributes basicFileAttributes;

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
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
