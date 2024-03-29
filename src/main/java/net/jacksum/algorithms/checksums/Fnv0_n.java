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

package net.jacksum.algorithms.checksums;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Fnv0_n extends AbstractChecksum {

    BigInteger[] BIG;
    protected BigInteger prime;
    protected BigInteger value = BigInteger.ZERO;
    protected BigInteger mask;

    int targetsize = 0; // in bytes

    public Fnv0_n(int width) throws NoSuchAlgorithmException {
        super();
        init(width);
    }

    public Fnv0_n(String width) throws NoSuchAlgorithmException {
        super();
        try {
            bitWidth = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            throw new NoSuchAlgorithmException(String.format("Unknown algorithm: not a number. %s", e));
        }
        init(bitWidth);
    }

    private void init(int width) throws NoSuchAlgorithmException {
        // check validity of the width
        if (width < 32 || width > 1024) {
            throw new NoSuchAlgorithmException(String.format("Unknown algorithm: width %s is not supported.", width));
        }
        this.bitWidth = width;

        // initialize formatPreferences
        formatPreferences.setSeparator(" ");
        if (width <= 32) {
            formatPreferences.setHashEncoding(Encoding.DEC);
            formatPreferences.setFilesizeWanted(true);
        } else {
            formatPreferences.setHashEncoding(Encoding.HEX);
            formatPreferences.setFilesizeWanted(false);
        }

        // initialize BigInteger array for faster access to the first
        // 255 BigInteger values
        BIG = new BigInteger[256];
        for (int i = 0; i < BIG.length; i++) {
            BIG[i] = BigInteger.valueOf(i);
        }

        // initialize BigInteger with the value of 2
        BigInteger TWO = BIG[2];

        // initialize members dependent on the width
        mask = TWO.pow(width).subtract(BigInteger.ONE);
        switch (width) {
            case 32:
                prime = TWO.pow(24);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x93]);
                // prime = 16777619
                // prime (bin) = 1000000000000000110010011
                // prime = new BigInteger("1000000000000000110010011", 2);
                break;
            case 64:
                prime = TWO.pow(40);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0xb3]);
                // prime = 1099511628211
                // prime (bin) = 10000000000000000000000000000000110110011
                // prime = new BigInteger("10000000000000000000000000000000110110011", 2);
                break;
            case 128:
                prime = TWO.pow(88);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x3b]);
                // prime = 309485009821345068724781371
                break;
            case 256:
                prime = TWO.pow(168);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x63]);
                break;
            case 512:
                prime = TWO.pow(344);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x57]);
                break;
            case 1024:
                prime = TWO.pow(680);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x8d]);
                break;
            default:
                throw new NoSuchAlgorithmException(String.format("Unknown algorithm: width %s is not supported.", width));
        }
        targetsize = width / 8;
    }


    @Override
    public void reset() {
        value = BigInteger.ZERO;
        length = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value = value.multiply(prime);
            value = value.and(mask);
            value = value.xor(BIG[bytes[i] & 0xFF]);
        }
        this.length += length;
    }


    @Override
    public byte[] getByteArray() {
        byte[] target = new byte[targetsize];
        byte[] source = value.and(mask).toByteArray();

        if (source.length > target.length) {
            int offset = 0;
            for (int i = 0; i < source.length; i++) {
                if (source[i] == 0) {
                    offset++;
                } else {
                    break;
                }
            }
            System.arraycopy(source, offset, target, 0, target.length);
        } else if (source.length <= target.length) {
            System.arraycopy(source, 0, target, target.length - source.length, source.length);
        }
        return target;
    }
}
