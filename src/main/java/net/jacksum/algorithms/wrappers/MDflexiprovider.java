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

package net.jacksum.algorithms.wrappers;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.zzadopt.de.flexiprovider.api.MessageDigest;
import net.jacksum.zzadopt.de.flexiprovider.core.md.DHA256;
import net.jacksum.zzadopt.de.flexiprovider.core.md.FORK256;
import net.jacksum.zzadopt.de.flexiprovider.core.md.VSH;

/**
 * A wrapper class that can be used to compute DSH-256, FORK-256, and VSH
 * (provided by flexiprovider.de (abandoned)).
 */
public class MDflexiprovider extends AbstractChecksum {

    private final static Map<String,Integer> blockSizesInBytes;
    static {
        blockSizesInBytes = new HashMap<>(3);
        blockSizesInBytes.put(DHA256.ALG_NAME, 64);
        blockSizesInBytes.put(FORK256.ALG_NAME, 64);
        blockSizesInBytes.put(VSH.ALG_NAME, 131);
    }

    
    protected MessageDigest md = null;
    protected boolean virgin = true;
    protected byte[] digest = null;
    
    private String arg = null;
    
    public MDflexiprovider() throws NoSuchAlgorithmException {
        
    }
    
    /**
     * Creates new MDbouncycastle.
     * @param arg a String.
     * @throws java.security.NoSuchAlgorithmException in case the algorithm is not supported.
     **/
    public MDflexiprovider(String arg) throws NoSuchAlgorithmException {
        this.arg = arg;
        // value0; we don't use value, we use md
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;
        
        if (arg.equalsIgnoreCase(DHA256.ALG_NAME)) {
            md = new DHA256();
        } else
        if (arg.equalsIgnoreCase(FORK256.ALG_NAME)) {
            md = new FORK256();
        } else
        if (arg.equalsIgnoreCase(VSH.ALG_NAME)) {
            md = new VSH();
        } else {
            throw new NoSuchAlgorithmException(arg + " is an unknown algorithm.");
        }
        
        bitWidth = md.getDigestLength()*8;
    }


    @Override
    public void reset() {
        md.reset();
        length = 0;
        virgin = true;
    }
    
    @Override
    public void update(byte[] buffer, int offset, int len) {
        md.update(buffer, offset, len);
        length += len;
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
    
    
    @Override
    public int getBlockSize() {
       return blockSizesInBytes.get(arg);
    }

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
