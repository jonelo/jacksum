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
import net.jacksum.algorithms.crcs.CrcUtils;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.FingerprintFormatter;
import net.jacksum.formats.Formatter;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.zzadopt.com.github.snksoft.crc.CRC;

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

        if (width < 1 || width > 63) {
            throw new ParameterException(String.format(
                    "Bit width %s is not supported by the CRC brute forcer, the supported range is [1..63].",
                    width));
        }
        if (parameters.getVerbose().isInfo()) {
            System.err.printf("Trying all CRC algorithms with a width of %s bits by brute force (be patient!) ...\n", width);
        }

        long maskAllBits = ~0L >>> (64 - width); // stores the value (2 ^ width) - 1

        boolean[] boolarray = {false, true};
        long[] inittab = {0L, maskAllBits};
        long[] xortab = {0L, maskAllBits};

        byte[] data = parameters.getSequence().asBytes();

        // the CRC of the input, indexed by [refIn][init], and the same value reflected;
        // both are filled once per polynomial and serve all 16 parameter combinations
        long[][] crc = new long[2][2];
        long[][] crcReflected = new long[2][2];

        int init;
        int refin;
        int refout;
        int xor;

        try {
            Expectation expectation = new Expectation(width);

            // the loop condition must not be poly <= maskAllBits, because for a width of 63
            // maskAllBits is Long.MAX_VALUE, so poly++ would wrap around instead of ending
            for (long poly = 0L;; poly++) {

                // the CRC table depends on width, poly and refIn only, so two tables are
                // sufficient for all 16 parameter combinations of this polynomial. The start
                // value of update() is init itself, because reflecting init changes nothing
                // for the two values that are tried, all bits zero and all bits one.
                for (refin = 0; refin < 2; refin++) {
                    CRC tableDriven = new CRC(new CRC.Parameters(width, poly, 0L,
                            boolarray[refin], boolarray[refin], 0L));
                    for (init = 0; init < 2; init++) {
                        crc[refin][init] = tableDriven.update(inittab[init], data);
                        crcReflected[refin][init] = CrcUtils.reflect(crc[refin][init], width);
                    }
                }

                for (init = 0; init < 2; init++) {
                    for (refin = 0; refin < 2; refin++) {
                        for (refout = 0; refout < 2; refout++) {
                            // refIn and refOut differ if and only if their indexes differ,
                            // the rest of the finalization is the xor below, see CRC.finalCRC()
                            long value = refout != refin ? crcReflected[refin][init] : crc[refin][init];
                            for (xor = 0; xor < 2; xor++) {
                                if (expectation.isMetBy((value ^ xortab[xor]) & maskAllBits)) {
                                    report(width, poly, inittab[init], boolarray[refin],
                                            boolarray[refout], xortab[xor], data);
                                }
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
     * Reports a candidate that produces the expected value. The comparison is performed again,
     * this time by the checksum object itself, so that a candidate is only reported if the
     * algorithm that is printed really produces the expected value.
     *
     * @param width the width in bits
     * @param poly the polynomial
     * @param init the initial value
     * @param refIn reflect the input bytes?
     * @param refOut reflect the output CRC?
     * @param xorOut the value that is xor'ed to the output CRC
     * @param data the input that the CRC has been calculated for
     * @throws NoSuchAlgorithmException if the parameters cannot be used to create a CRC
     */
    private void report(int width, long poly, long init, boolean refIn, boolean refOut, long xorOut,
            byte[] data) throws NoSuchAlgorithmException {
        AbstractChecksum checksum = new CrcGeneric(width, poly, init, refIn, refOut, xorOut);
        checksum.setParameters(parameters);
        checksum.update(data);

        CompareAndFindAlgo action = new CompareAndFindAlgo(checksum, parameters);
        action.perform();
        found += action.getPositives();
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

    /**
     * The value that the user expects, prepared for a comparison against the value of a CRC
     * candidate. Everything that does not depend on the candidate is done once, so that the
     * comparison itself does not have to create a checksum object.
     */
    private class Expectation {

        // the expected value, if it can be compared as a number
        private final long expected;
        private final boolean expectedIsNumber;
        // true if no candidate can ever match, e.g. because the expected value has a length
        // that a CRC of that width cannot produce
        private final boolean impossible;
        // the state for a comparison of the formatted values, see CompareAction.equalsTolerant()
        private final byte[] bytes;
        private final FingerprintFormatter fingerprintFormatter;
        private final Encoding encoding;
        private final String expectedAsString;

        Expectation(int width) throws NoSuchAlgorithmException {
            // the number of bytes that a CRC of that width occupies,
            // see CrcGeneric.getByteArray()
            int hashBytes = width / 8 + ((width % 8 > 0) ? 1 : 0);

            byte[] expectedBytes;
            try {
                expectedBytes = parameters.getExpectedBytes();
            } catch (UnsupportedOperationException e) {
                // the encoding cannot be decoded, so the formatted values have to be compared
                expectedBytes = null;
            }

            if (expectedBytes != null) {
                expectedIsNumber = true;
                impossible = expectedBytes.length != hashBytes;
                long value = 0L;
                if (!impossible) {
                    for (byte b : expectedBytes) {
                        value = (value << 8) | (b & 0xFF);
                    }
                }
                expected = value;
                bytes = null;
                fingerprintFormatter = null;
                encoding = null;
                expectedAsString = null;
            } else {
                expectedIsNumber = false;
                impossible = false;
                expected = 0L;
                bytes = new byte[hashBytes];
                // any CRC of that width formats its value in the same way, so one object
                // is sufficient to format the value of every candidate
                CrcGeneric prototype = new CrcGeneric(width, 0L, 0L, false, false, 0L);
                prototype.setParameters(parameters);
                fingerprintFormatter = new Formatter(prototype.getFormatPreferences()).getFingerprintFormatter();
                encoding = prototype.getFormatPreferences().getEncoding();
                expectedAsString = parameters.getExpectedString();
            }
        }

        /**
         * Is the value the value that the user expects?
         *
         * @param value the value of a CRC candidate
         * @return true if the value is the expected one
         */
        boolean isMetBy(long value) {
            if (impossible) {
                return false;
            }
            if (expectedIsNumber) {
                return value == expected;
            }
            for (int i = bytes.length - 1; i > -1; i--) {
                bytes[i] = (byte) (value & 0xFF);
                value >>>= 8;
            }
            return Encoding.hashesAreEqual(fingerprintFormatter.format(bytes), expectedAsString, encoding);
        }
    }

}
