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

/* 
 * (PD) 2003 The Bitzi Corporation
 * Please see http://bitzi.com/publicdomain for more info.
 *
 * Basis:
 * TigerTree.java,v 1.9 2006/06/29 23:35:00 sberlin Exp $
 * see also
 * https://www.limewire.org/fisheye/browse/limecvs/core/com/limegroup/gnutella/security/TigerTree.java
 *
 * Modifications by jonelo for Jacksum (GPL):
 * - changed the package name
 * - replaced the MessageDigest with Jacksum's AbstractChecksum
 * - provided a named constructor to be able to calculate both tiger and tiger2
 * - provided getBlockSize to be able to perform calculations with any Hash algorithm that reveals its block size
 * - replaced the cryptix Tiger provider with GNU's implementation
 * - code reformatted
 * - fixed sf change request# 1693872: Decrease the memory requirement of the TigerTree class
 *   http://sourceforge.net/tracker/?func=detail&aid=1693872&group_id=74387&atid=540849
 *   now at https://sourceforge.net/p/jacksum/feature-requests/13/
 */
package net.jacksum.algorithms.wrappers;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * Implementation of THEX tree hash algorithm, with any hash algorithm (using
 * the approach as revised in December 2002, to add unique prefixes to leaf and
 * node operations)
 *
 * This more space-efficient approach uses a stack, and calculate each node as
 * soon as its children ara available.
 */
public class TigerTree extends MessageDigest {

    private final static int BLOCKSIZE = 1024;
    private final int HASHSIZE; // = 24;
    /**
     * a Marker for the Stack
     */
    private final byte[] MARKER = new byte[0];
    /**
     * 1024 byte buffer
     */
    private final byte[] buffer;
    /**
     * Buffer offset
     */
    private int bufferOffset;
    /**
     * Number of bytes hashed until now.
     */
    private long byteCount;
    /**
     * Internal Tiger MD instance
     */
    private AbstractChecksum algo;
    /**
     * Interim tree node hash values
     */
    private final ArrayList<byte[]> nodes; // contains <byte[]>

    /**
     * Constructor
     *
     * @param name the name.
     * @throws java.security.NoSuchAlgorithmException if there is no such algorithm.
     */
    public TigerTree(String name) throws NoSuchAlgorithmException {
        super(name);
        
        
        buffer = new byte[BLOCKSIZE];
        bufferOffset = 0;
        byteCount = 0;
        algo = JacksumAPI.getChecksumInstance(name);
        int algoSize = algo.getSize();
        if (algoSize == 0) {
            throw new NoSuchAlgorithmException("The Tree Hash requires an algorithm that supports the implementation of getSize()");
        }
        if (algoSize % 8 != 0) {
            throw new NoSuchAlgorithmException("The Tree Hash requires an algorithm with a length of 8 bit multiples.");
        }
        HASHSIZE = algoSize / 8;
        nodes = new ArrayList<>();
    }

    @Override
    protected int engineGetDigestLength() {
        return HASHSIZE;
    }

    public int getBlockSize() {
        return algo.getBlockSize();
    }

    @Override
    protected void engineUpdate(byte in) {
        byteCount += 1;
        buffer[bufferOffset++] = in;
        if (bufferOffset == BLOCKSIZE) {
            blockUpdate();
            bufferOffset = 0;
        }
    }

    @Override
    protected void engineUpdate(byte[] in, int offset, int length) {
        byteCount += length;
        nodes.ensureCapacity(log2Ceil(byteCount / BLOCKSIZE));

        if (bufferOffset > 0) {
            int remaining = BLOCKSIZE - bufferOffset;
            System.arraycopy(in, offset, buffer, bufferOffset, remaining);
            blockUpdate();
            bufferOffset = 0;
            length -= remaining;
            offset += remaining;
        }

        while (length >= BLOCKSIZE) {
            blockUpdate(in, offset, BLOCKSIZE);
            length -= BLOCKSIZE;
            offset += BLOCKSIZE;
        }

        if (length > 0) {
            System.arraycopy(in, offset, buffer, 0, length);
            bufferOffset = length;
        }
    }

    @Override
    protected byte[] engineDigest() {
        byte[] hash = new byte[HASHSIZE];
        try {
            engineDigest(hash, 0, HASHSIZE);
        } catch (DigestException e) {
            return null;
        }
        return hash;
    }

    @Override
    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        if (len < HASHSIZE) {
            throw new DigestException();
        }

        // hash any remaining fragments
        blockUpdate();

        byte[] ret = collapse();

        // Assert.that(ret != MARKER);
        System.arraycopy(ret, 0, buf, offset, HASHSIZE);
        engineReset();
        return HASHSIZE;
    }

    /**
     * collapse whatever the tree is now to a root.
     */
    private byte[] collapse() {
        byte[] last = null;
        for (int i = 0; i < nodes.size(); i++) {
            byte[] current = (byte[]) nodes.get(i);
            if (current == MARKER) {
                continue;
            }

            if (last == null) {
                last = current;
            } else {
                algo.reset();
                algo.update((byte) 1); // node prefix
                algo.update(current);
                algo.update(last);
                last = algo.getByteArray();
            }

            nodes.set(i, MARKER);
        }
        // Assert.that(last != null);
        return last;
    }

    @Override
    protected void engineReset() {
        bufferOffset = 0;
        byteCount = 0;
        nodes.clear();
        algo.reset();
    }

    /**
     * Method overrides MessageDigest.clone()
     *
     * @throws java.lang.CloneNotSupportedException if clone() is not supported.
     * @see java.security.MessageDigest#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected void blockUpdate() {
        blockUpdate(buffer, 0, bufferOffset);
    }

    /**
     * Update the internal state with a single block of size 1024 (or less, in
     * final block) from the internal buffer.
     *
     * @param buf the byte buffer.
     * @param pos the position.
     * @param len the length.
     */
    protected void blockUpdate(byte[] buf, int pos, int len) {
        algo.reset();
        algo.update((byte) 0); // leaf prefix
        algo.update(buf, pos, len);
        if ((len == 0) && (nodes.size() > 0)) {
            return; // don't remember a zero-size hash except at very beginning
        }
        byte[] digest = algo.getByteArray();
        push(digest);
    }

    private void push(byte[] data) {
        if (!nodes.isEmpty()) {
            for (int i = 0; i < nodes.size(); i++) {
                byte[] node = (byte[]) nodes.get(i);
                if (node == MARKER) {
                    nodes.set(i, data);
                    return;
                }

                algo.reset();
                algo.update((byte) 1);
                algo.update(node);
                algo.update(data);
                data = algo.getByteArray();
                nodes.set(i, MARKER);
            }
        }
        nodes.add(data);
    }

    // calculates the next n with 2^n > number
    public static int log2Ceil(long number) {
        int n = 0;
        while (number > 1) {
            number++; // for rounding up.
            number >>>= 1;
            n++;
        }
        return n;
    }
}
