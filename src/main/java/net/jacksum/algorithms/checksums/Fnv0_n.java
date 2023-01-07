/*


  Jacksum 3.5.0 - a checksum utility in Java
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
    protected BigInteger modulo;
    //protected int width = 0;
    int targetsize = 0; // in bytes

    public Fnv0_n(int width) throws NoSuchAlgorithmException {
        super();
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setFilesizeWanted(true);
        formatPreferences.setSeparator(" ");
        init(width);
    }

    private void init(int width) throws NoSuchAlgorithmException {
        if ((width < 32) || (width > 1024)) {
            throw new NoSuchAlgorithmException("Unknown algorithm: width "
                + width + " is not supported.");
        }
        this.bitWidth = width;
        BIG = new BigInteger[256];
        for (int i=0; i < BIG.length; i++) {
            BIG[i] = BigInteger.valueOf(i);
        }
        if (width <= 32) {
            formatPreferences.setHashEncoding(Encoding.DEC);
        } else {
            formatPreferences.setHashEncoding(Encoding.HEX);
        }
        BigInteger TWO = BIG[2]; // BigInteger.valueOf(2);
        modulo = TWO.pow(width);
        switch (width) {
            case 32:
                targetsize = 4;
                prime = TWO.pow(24);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x93]);
                // prime = 16777619
                break;
            case 64:
                targetsize = 8;
                prime = TWO.pow(40);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0xb3]);
                // prime = 1099511628211
                break;
            case 128:
                targetsize = 16;
                prime = TWO.pow(88);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x3b]);
                // prime = 309485009821345068724781371
                break;
            case 256:
                targetsize = 32;
                prime = TWO.pow(168);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x63]);
                break;
            case 512:
                targetsize = 64;
                prime = TWO.pow(344);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x57]);
                break;
            case 1024:
                targetsize = 128;
                prime = TWO.pow(680);
                prime = prime.add(TWO.pow(8));
                prime = prime.add(BIG[0x8d]);
                break;
            default:
                throw new NoSuchAlgorithmException(
                        "Unknown algorithm: width " + width + " is not supported.");
        }
    }

    public Fnv0_n(String strwidth) throws NoSuchAlgorithmException {
        super();
        try {
            bitWidth = Integer.parseInt(strwidth);
        } catch (NumberFormatException e) {
            throw new NoSuchAlgorithmException(
                    "Unknown algorithm: not a number. " + e);
        }
        init(bitWidth);
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
            value = value.mod(modulo);
            value = value.xor(BIG[bytes[i] & 0xFF]);
        }
        this.length += length;
    }


    @Override
    public byte[] getByteArray() {
        byte[] target = new byte[targetsize];
        byte[] source = value.and(modulo.subtract(BigInteger.ONE)).toByteArray();

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
        } else
        if (source.length <= target.length) {
            System.arraycopy(source, 0, target, target.length - source.length, source.length);
        }
        return target;
    }
}
