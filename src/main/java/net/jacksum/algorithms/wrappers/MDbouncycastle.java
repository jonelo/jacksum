/*
 *
 * Jacksum 4.0.2 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 */

package net.jacksum.algorithms.wrappers;

import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.crypto.engines.GOST28147Engine;
import net.jacksum.zzadopt.org.bouncycastle.crypto.digests.TigerDigest_192_4_PHP_version;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
//import org.bouncycastle.crypto.digests.Haraka256Digest;
//import org.bouncycastle.crypto.digests.Haraka512Digest;

/**
 * A wrapper class that can be used to compute GOST, RIPEMD256 and RIPEMD320, etc.
 * (provided by bouncycastle.org).
 */
public class MDbouncycastle extends AbstractChecksum {
   
    
    /** The wrapped Bouncy Castle digest. */
    protected Digest md = null;
    /** Whether the digest still has to be computed, i.e. it has not been finalized since the last reset. */
    protected boolean virgin = true;
    /** The computed digest, or null if it has not been computed yet. */
    protected byte[] digest = null;
    private int newDigestWidthInBits = -1;
    /** The output length in bytes of an XOF that is used with a fixed output length, or -1 if not set. */
    private int xofOutputLengthInBytes = -1;
    
    /**
     * Creates a new instance without a digest; intended for subclasses that
     * set {@link #md} themselves.
     *
     * @throws NoSuchAlgorithmException never thrown by this constructor
     */
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
            md = new Kangaroo.KangarooTwelve();
            xofOutputLengthInBytes = 32;
        } else

        if (arg.equalsIgnoreCase("marsupilamifourteen")) { // 256-bit security strength
            md = new Kangaroo.MarsupilamiFourteen();
            xofOutputLengthInBytes = 64;
        } else

        if (arg.equalsIgnoreCase("tiger-192-4-php")) {
            md = new TigerDigest_192_4_PHP_version();
        } else

        if (arg.equalsIgnoreCase("tiger-160-4-php")) {
            md = new TigerDigest_192_4_PHP_version();
            newDigestWidthInBits = 160;
        } else

        if (arg.equalsIgnoreCase("tiger-128-4-php")) {
            md = new TigerDigest_192_4_PHP_version();
            newDigestWidthInBits = 128;
        } else

        if (arg.equalsIgnoreCase("blake3")) {
            md = new Blake3Digest(256);
        } else

        if (arg.equalsIgnoreCase("ascon-hash")) {
            md = new AsconDigest(AsconDigest.AsconParameters.AsconHash);
        } else

        if (arg.equalsIgnoreCase("ascon-hasha")) {
            md = new AsconDigest(AsconDigest.AsconParameters.AsconHashA);
        } else

        if (arg.equalsIgnoreCase("ascon-xof")) {
            md = new AsconXof(AsconXof.AsconParameters.AsconXof);
        } else

        if (arg.equalsIgnoreCase("ascon-xofa")) {
            md = new AsconXof(AsconXof.AsconParameters.AsconXofA);
        } else

        if (arg.equalsIgnoreCase("esch256")) {
            md = new SparkleDigest(SparkleDigest.SparkleParameters.ESCH256);
        } else
        if (arg.equalsIgnoreCase("esch384")) {
            md = new SparkleDigest(SparkleDigest.SparkleParameters.ESCH384);
        } else

        if (arg.equalsIgnoreCase("photon-beetle")) {
            md = new PhotonBeetleDigest();
        } else

        if (arg.equalsIgnoreCase("xoodyak")) {
            md = new XoodyakDigest();
        } else

        throw new NoSuchAlgorithmException(arg + " is an unknown algorithm.");
        if (newDigestWidthInBits > 0) {
            bitWidth = newDigestWidthInBits;
        } else if (xofOutputLengthInBytes > 0) {
            bitWidth = xofOutputLengthInBytes * 8;
        } else {
            bitWidth = md.getDigestSize() * 8;
        }
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
        if (md instanceof ExtendedDigest) {
            return ((ExtendedDigest)md).getByteLength();
        } else {
            return -1;
        }
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            if (xofOutputLengthInBytes > 0) {
                // since BC 1.71 the Kangaroo constructors ignore the requested output length,
                // so we request the output length explicitly from the XOF
                digest = new byte[xofOutputLengthInBytes];
                ((Xof)md).doFinal(digest, 0, xofOutputLengthInBytes);
            } else {
                digest = new byte[md.getDigestSize()];
                md.doFinal(digest, 0);
            }
            virgin = false;
        }
        // we don't expose internal representations
        int digestWidthInBytes = 0;
        if (newDigestWidthInBits > 0) {
            digestWidthInBytes = (newDigestWidthInBits / 8) + (newDigestWidthInBits % 8 > 0 ? 1 : 0);
        } else {
            digestWidthInBytes = digest.length;
        }
        byte[] save = new byte[digestWidthInBytes];
        System.arraycopy(digest, 0, save, 0, digestWidthInBytes);
        return save;
    }
    
}
