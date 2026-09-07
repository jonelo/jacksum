/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
 * @author Johann N. Löfflmann
 */
public class BruteForceCRC implements FindAlgoEngine {

    // the number of parameter combinations that are tried for each polynomial:
    // init, refIn, refOut and xorOut are varied over two values each
    private static final int COMBINATIONS_PER_POLY = 16;

    // the number of polynomials after which the polynomial counter is transferred
    // to searched, so that the counter cannot overflow
    private static final long FLUSH_AT = 1L << 40;

    private final Parameters parameters;
    private BigInteger searched = BigInteger.ZERO;
    private long polysDone;
    private long found;
    
    public BruteForceCRC(Parameters parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public void find(int width) throws ParameterException {

        if (width < 8 || width > 63) {
            throw new ParameterException(String.format(
                    "Bit width %s is not supported by the CRC brute forcer, the supported range is [8..63].",
                    width));
        }
        if (parameters.getVerbose().isInfo()) {
            System.err.printf("Trying all CRC algorithms with a width of %s bits by brute force (be patient!) ...\n", width);
        }

        AbstractChecksum checksum;

        long maskAllBits = ~0L >>> (64 - width); // stores the value (2 ^ width) - 1

        boolean[] boolarray = {false, true};
        long[] inittab = {0L, maskAllBits};
        long[] xortab = {0L, maskAllBits};

        int init;
        int refin;
        int refout;
        int xor;

        try {
            // the loop condition must not be poly <= maskAllBits, because for a width of 63
            // maskAllBits is Long.MAX_VALUE, so poly++ would wrap around instead of ending
            for (long poly = 0L;; poly++) {
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
                            }
                        }
                    }
                }
                if (++polysDone == FLUSH_AT) {
                    searched = searched.add(polysAsCandidates());
                    polysDone = 0;
                }
                if (poly == maskAllBits) {
                    break;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ParameterException(e.getMessage());
        }
    }
    
    /**
     * @return the number of candidates that the polynomials counted in polysDone stand for
     */
    private BigInteger polysAsCandidates() {
        return BigInteger.valueOf(polysDone).multiply(BigInteger.valueOf(COMBINATIONS_PER_POLY));
    }

    /**
     * @return the found
     */
    @Override
    public long getFound() {
        return found;
    }

    /**
     * @return the searched
     */
    @Override
    public BigInteger getSearched() {
        return searched.add(polysAsCandidates());
    }


}
