/** 
 *******************************************************************************
 *
 * Jacksum 3.5.0 - a checksum utility in Java
 * Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.algorithms.wrappers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A wrapper class that can be used to compute MD5, SHA-1, SHA-256, SHA-384 and SHA-512
 * (provided by your JRE/JDK vendor) and MDC2 for example
 */
public class MD extends AbstractChecksum {

    private MessageDigest md = null;
    private boolean virgin = true;
    private byte[] digest = null;

    // instance initialization block
    // for all constructors
    {
        length = 0;
        filename = null;
        virgin = true;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
    }

    public MD(String arg, int blocksize) throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance(arg);
        bitWidth = md.getDigestLength() * 8;
        this.blocksize = blocksize;
    }
    
    public MD(MessageDigest md, int blocksize) {
        this.md = md;
        bitWidth = md.getDigestLength() * 8;
        this.blocksize = blocksize;
    }
    

    @Override
    public void reset() {
        md.reset();
        length = 0;
        virgin = true;
    }
    
    /*
    public int getBlockSize() {
        if (md.getAlgorithm().equalsIgnoreCase("SHA-1"))   return 64; else
        if (md.getAlgorithm().equalsIgnoreCase("MD5"))     return 64; else
        if (md.getAlgorithm().equalsIgnoreCase("SHA-256")) return 64; else
        if (md.getAlgorithm().equalsIgnoreCase("SHA-384")) return 128; else
        if (md.getAlgorithm().equalsIgnoreCase("SHA-512")) return 128; else
        if (md.getAlgorithm().equalsIgnoreCase("MDC2"))    return 64; else
        return 0;
    }*/
    
    @Override
    public int getSize() {
        return md.getDigestLength() * 8;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        md.update(buffer,offset,len);
        length+=len;
    }

    @Override
    public void update(byte b) {
        md.update(b);
        length++;
    }

    @Override
    public void update(int b) {
        update((byte)(b & 0xFF));
    }

    /*
    @Override
    public String toString() {
        
        return getValueFormatted()
                + (isTimestampWanted() ? formatPreferences.getSeparator() + getTimestampFormatted() : "")
                + (isFilenameWanted() ? formatPreferences.getSeparator() + getFilename() : "");
    }*/

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = md.digest();
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }

}
