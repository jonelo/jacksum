/*


  Jacksum 3.8.0 - a checksum utility in Java
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
package net.jacksum.actions.io.findalgo.engines;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import net.jacksum.actions.io.compare.CompareAndFindAlgo;
import net.jacksum.actions.io.findalgo.FindAlgoEngine;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.crcs.CrcGeneric;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;

/**
 *
 * @author Johann
 */
public class BruteForceCRC implements FindAlgoEngine {


    private final Parameters parameters;
    private BigInteger searched = BigInteger.ONE;
    private long found;
    
    public BruteForceCRC(Parameters parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public void find(int width) throws ParameterException {

        if (width < 8 || width > 63) { // || (width % 8 > 0)) {
            throw new ParameterException("Bit width " + width + " is not supported by the CRC brute forcer.");
        }
        if (parameters.getVerbose().isInfo()) {
            System.err.printf("Trying all CRC algorithms with a width of %s bits by brute force (be patient!) ...\n", width);
        }

        AbstractChecksum checksum;

        long maskAllBits = ~0L >>> (64 - width); // stores the value (2 ^ width) - 1
        //long maskAllBits = width < 64 ? (1L << width) - 1 : ~0L; 

        boolean[] boolarray = {false, true};
        long[] inittab = {0L, maskAllBits};
        long[] xortab = {0L, maskAllBits};

        long poly;
        int init;
        int refin;
        int refout;
        int xor;

        try {
            if (width < 64) {
                for (poly = 0L; poly <= maskAllBits; poly++) {
                    for (init = 0; init < 2; init++) {
                        for (refin = 0; refin < 2; refin++) {
                            for (refout = 0; refout < 2; refout++) {
                                for (xor = 0; xor < 2; xor++) {
                                    checksum = new CrcGeneric(width,
                                            poly,
                                            inittab[init],
                                            boolarray[refin],
                                            boolarray[refout],
                                            xortab[xor]);
                                    checksum.setParameters(parameters);
                                    checksum.update(parameters.getSequence().asBytes());

                                    CompareAndFindAlgo action = new CompareAndFindAlgo(checksum, parameters);
                                    action.perform();
                                    found += action.getPositives();
                                    searched = getSearched().add(BigInteger.ONE);
                                }
                            }
                        }
                    }
                }

            } else {
                //  throw new UnsupportedOperationException(""));
                // TODO: not yet implemented, because Java doesn't support 64 bit unsinged long
                // we have to do it using BigInteger for width == 64
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ParameterException("No such algorithm.");
        }
    }
    
    /**
     * @return the found
     */
    public long getFound() {
        return found;
    }

    /**
     * @return the searched
     */
    @Override
    public BigInteger getSearched() {
        return searched;
    }


}
