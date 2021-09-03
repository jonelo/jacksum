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
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.digests.RIPEMD320Digest;
import org.bouncycastle.crypto.engines.GOST28147Engine;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import org.bouncycastle.crypto.digests.DSTU7564Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_512Digest;
import org.bouncycastle.crypto.digests.Kangaroo;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512tDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.digests.SM3Digest;
//import org.bouncycastle.crypto.digests.Haraka256Digest;
//import org.bouncycastle.crypto.digests.Haraka512Digest;

/**
 * A wrapper class that can be used to compute GOST, RIPEMD256 and RIPEMD320
 * (provided by bouncycastle.org).
 */
public class MDbouncycastle extends AbstractChecksum {
   
    
    protected ExtendedDigest md = null;
    protected boolean virgin = true;
    protected byte[] digest = null;
    
    
    public MDbouncycastle() throws NoSuchAlgorithmException {
        
    }
    
    /** Creates new MDbouncycastle
     * @param arg the name of the algorithm.
     * @throws java.security.NoSuchAlgorithmException if the algorithm is not supported.
     **/
    public MDbouncycastle(String arg) throws NoSuchAlgorithmException {
        // value0; we don't use value, we use md
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;
        
        if (arg.equalsIgnoreCase("sha3-512")) {
            md = new SHA3Digest(512);
        } else
        if (arg.equalsIgnoreCase("sha3-384")) {
            md = new SHA3Digest(384);
        } else
        if (arg.equalsIgnoreCase("sha3-256")) {
            md = new SHA3Digest(256);
        } else
        if (arg.equalsIgnoreCase("sha3-224")) {
            md = new SHA3Digest(224);
        } else

        if (arg.equalsIgnoreCase("sha512/224")) {
            md = new SHA512tDigest(224);
        } else
        if (arg.equalsIgnoreCase("sha512/256")) {
            md = new SHA512tDigest(256);
        } else
            
        if (arg.equalsIgnoreCase("sm3")) {
            md = new SM3Digest();
        } else

        if (arg.equalsIgnoreCase("shake128")) {
            md = new SHAKEDigest(128);            
        } else
        if (arg.equalsIgnoreCase("shake256")) {
            md = new SHAKEDigest(256);
        } else

            
        if (arg.equalsIgnoreCase("keccak-224")) {
            md = new KeccakDigest(224);
        } else
        if (arg.equalsIgnoreCase("keccak-256")) {
            md = new KeccakDigest(256);
        } else
        if (arg.equalsIgnoreCase("keccak-288")) {
            md = new KeccakDigest(288);
        } else
        if (arg.equalsIgnoreCase("keccak-384")) {
            md = new KeccakDigest(384);
        } else
        if (arg.equalsIgnoreCase("keccak-512")) {
            md = new KeccakDigest(512);
        } else

        if (arg.equalsIgnoreCase("streebog-512")) {
            md = new GOST3411_2012_512Digest();
        } else
        if (arg.equalsIgnoreCase("streebog-256")) {
            md = new GOST3411_2012_256Digest();
        } else
            
        if (arg.equalsIgnoreCase("gost:crypto-pro")) {
            md = new GOST3411Digest(GOST28147Engine.getSBox("D-A"));
        } else
        if (arg.equalsIgnoreCase("gost:default")) {
            md = new GOST3411Digest(GOST28147Engine.getSBox("Default"));
        } else

        if (arg.equalsIgnoreCase("ripemd320")) {
            md = new RIPEMD320Digest();
        } else
        if (arg.equalsIgnoreCase("ripemd256")) {
            md = new RIPEMD256Digest(); 
        } else

        if (arg.equalsIgnoreCase("kupyna-512")) {
            md = new DSTU7564Digest(512);
        } else
        if (arg.equalsIgnoreCase("kupyna-384")) {
            md = new DSTU7564Digest(384);
        } else
        if (arg.equalsIgnoreCase("kupyna-256")) {
            md = new DSTU7564Digest(256);
        } else

        if (arg.equalsIgnoreCase("kangarootwelve")) { // 128-bit security strength
            md = new Kangaroo.KangarooTwelve(32); // parameter in bytes, not bits
        } else

        if (arg.equalsIgnoreCase("marsupilamifourteen")) { // 256-bit security strength
            md = new Kangaroo.MarsupilamiFourteen(64); // parameter in bytes, not bits
        } else
            
/*
        if (arg.equalsIgnoreCase("haraka-512")) {
            md = new Haraka512Digest();
        } else            
        if (arg.equalsIgnoreCase("haraka-256")) {
            md = new Haraka256Digest();
        } else
*/
            
        throw new NoSuchAlgorithmException(arg + " is an unknown algorithm.");
        bitWidth = md.getDigestSize()*8;
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
       return md.getByteLength();
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = new byte[md.getDigestSize()];
            md.doFinal(digest, 0);
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }
    
}
