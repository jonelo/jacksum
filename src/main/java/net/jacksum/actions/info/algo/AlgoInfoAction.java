/*


  Jacksum 3.7.0 - a checksum utility in Java
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
package net.jacksum.actions.info.algo;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import net.jacksum.algorithms.HMAC;
import net.jacksum.algorithms.checksums.PrngHashInfo;
import net.jacksum.algorithms.crcs.CrcInfo;
import net.jacksum.algorithms.crcs.CrcUtils;
import net.loefflmann.sugar.util.ByteSequences;
import net.loefflmann.sugar.util.ExitException;
import net.jacksum.JacksumAPI;
import net.jacksum.actions.Action;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.CombinedChecksum;
import net.jacksum.algorithms.crcs.CrcGeneric;
import net.jacksum.cli.ExitCode;
import net.jacksum.multicore.manyalgos.HashAlgorithm;

public class AlgoInfoAction implements Action {

    private final AlgoInfoActionParameters parameters;
    private AlgoInfoActionStatistics statistics;
    private int algorithms;
    private int allSupportedAlgorithmsCount;

    int maxWeight = 0;

    public AlgoInfoAction(AlgoInfoActionParameters parameters) {
        this.parameters = parameters;
        this.statistics = new AlgoInfoActionStatistics();
    }

    private static final String FORMAT = "%s  %-38s%s%n";

    private void printAlgoInfo(StringBuilder buffer, int indentation, AbstractChecksum checksum) {
        String indent = "  ";
        /*            if (indentation == 0) {
                indent = "";
            } else {
                indent = String.format("%"+indentation+"s", "");
            }*/

        buffer.append(String.format("%shash length:%n", indent));
        buffer.append(String.format(FORMAT, indent, "bits:", checksum.getSize()));
        int padOneByte = checksum.getSize() % 8 > 0 ? 1 : 0;
        int bytes = checksum.getSize() / 8 + padOneByte;
        if (padOneByte > 0) {
            buffer.append(String.format(FORMAT, indent, "bits (zero padded to 8-bit bytes):", bytes * 8));
        }
        buffer.append(String.format(FORMAT, indent, "bytes:", bytes));
        buffer.append(String.format(FORMAT, indent, "nibbles:", bytes * 2));

        int blockSize = checksum.getBlockSize();
        if (blockSize > 0) {
            buffer.append(String.format("%n%sblocksize:%n", indent));
            buffer.append(String.format(FORMAT, indent, "bits:", Integer.toString(blockSize * 8)));
            buffer.append(String.format(FORMAT, indent, "bytes:", Integer.toString(blockSize)));
        }

        if (checksum instanceof PrngHashInfo) {
            PrngHashInfo prngHashinfo = (PrngHashInfo) checksum;
            buffer.append(String.format("%n%sPRNG Hash parameters:%n", indent));
            buffer.append(String.format(FORMAT, indent, "Jacksum PRNG Hash algo def:", checksum.getName()));
            buffer.append(String.format(FORMAT, indent, "init [hex]:", "0x"+ByteSequences.hexformat(prngHashinfo.getInitValue(), 8)));
            buffer.append(String.format(FORMAT, indent, "multiplier [hex]:", "0x"+ByteSequences.hexformat(prngHashinfo.getMultiplier(), 8)));
            buffer.append(String.format(FORMAT, indent, "add [hex]:", "0x"+ByteSequences.hexformat(prngHashinfo.getAdd(), 8)));
        }

        if (checksum instanceof HMAC) {
            HMAC hmac = (HMAC) checksum;
            buffer.append(String.format("%n%sHMAC parameters:%n", indent));
            buffer.append(String.format(FORMAT, indent, "underlying cryptographic hash:", hmac.getAlgorithm().getName()));

            // RFC 2104:
            // We recommend that the output length t be not less than half the length of the hash
            // output (to match the birthday attack bound) and not less than 80 bits
            // (a suitable lower bound on the number of bits that need to be
            // predicted by an attacker).
            int minRecomTruncLength = Math.max(hmac.getSize() / 2, 80);
            buffer.append(String.format(FORMAT, indent, "truncate to bits:", hmac.getOutputLengthInBits() > 0 ? hmac.getOutputLengthInBits() : "no truncation"));
            buffer.append(String.format(FORMAT, indent, "truncate to bytes:", hmac.getOutputLengthInBits() > 0 ? hmac.getOutputLengthInBits()/8 + (hmac.getOutputLengthInBits() % 8 > 0 ? 1 : 0): "no truncation"));
            buffer.append(String.format(FORMAT, indent, "trunc. length should have min. bits:", minRecomTruncLength));
            buffer.append(String.format(FORMAT, indent, "trunc. length follows above recom.:", hmac.getOutputLengthInBits() > 0 ? Boolean.valueOf(hmac.getOutputLengthInBits() >= minRecomTruncLength).toString() : "no truncation"));
            buffer.append(String.format(FORMAT, indent, "key length should have min. bytes:", hmac.getAlgorithm().getSize()/8));
            buffer.append(String.format(FORMAT, indent, "key length follows above recom.:", hmac.isKeyLengthMatchedRecommendedMinimum()));
            buffer.append(String.format(FORMAT, indent, "key will be hashed:", hmac.isKeyWasHashed()));
        }

        if (checksum instanceof CrcInfo) {
            CrcInfo crc = (CrcInfo) checksum;
            byte[] polyAsBytes = crc.getPolyAsBytes();
            String polyAsBits = ByteSequences.formatAsBits(polyAsBytes, crc.getWidth());
            String reversedPolyAsBits = new StringBuilder(polyAsBits).reverse().toString();
            String koopmanPolyAsBits = "1"+polyAsBits.substring(0, polyAsBits.length()-1);
            String reciprocalPolyAsBits = new StringBuilder(koopmanPolyAsBits).reverse().toString();

            buffer.append(String.format("%n%sCRC parameters:%n", indent));
            buffer.append(String.format(FORMAT, indent, "width (in bits):", crc.getWidth()));
            buffer.append(String.format(FORMAT, indent, "polynomial [hex]:", new BigInteger(polyAsBits, 2).toString(16)));
            buffer.append(String.format(FORMAT, indent, "init [hex]:", ByteSequences.hexformat(crc.getInitialValue(), crc.getWidth() / 4)));
            buffer.append(String.format(FORMAT, indent, "refIn:", crc.isRefIn()));
            buffer.append(String.format(FORMAT, indent, "refOut:", crc.isRefOut()));
            buffer.append(String.format(FORMAT, indent, "xorOut [hex]:", ByteSequences.hexformat(crc.getXorOut(), crc.getWidth() / 4)));

            if (checksum instanceof CrcGeneric) {
                CrcGeneric crcGen = (CrcGeneric) checksum;

                // only if we don't overwrite any methods in CrcGeneric we can be sure that the object can be fully constructed again
                // by the value for "Jacksum CRC algo def"
                if (!crcGen.isTainted()) {
                    if (crcGen.getModel().isIncludeLength()) {
                        buffer.append(String.format(FORMAT, indent, "incLenMSO:", crcGen.getModel().isIncludeLengthMSOfirst()));
                        if (crcGen.getModel().isXorLength()) {
                            byte[] xorLengthArray = crcGen.getModel().getXorLengthArray();
                            if (xorLengthArray != null) {
                                buffer.append(String.format(FORMAT, indent, "xorLen [hex]:", ByteSequences.format(xorLengthArray)));
                            }
                        }
                    }
                    buffer.append(String.format(FORMAT, indent, "Jacksum CRC algo def:", crcGen.getString()));
                }
            }

            buffer.append(String.format("%n%sPolynomial representations:%n", indent));
            buffer.append(String.format(FORMAT, indent, "mathematical:", CrcUtils.polyAsMathExpression(crc.getWidth(), polyAsBytes)));
            buffer.append(String.format(FORMAT, indent, "normal/MSB first [binary]:", polyAsBits));
            buffer.append(String.format(FORMAT, indent, "normal/MSB first [hex]:", new BigInteger(polyAsBits, 2).toString(16)));
            buffer.append(String.format(FORMAT, indent, "reversed/LSB first [binary]:", reversedPolyAsBits));
            buffer.append(String.format(FORMAT, indent, "reversed/LSB first [hex]:", new BigInteger(reversedPolyAsBits, 2).toString(16)));
            buffer.append(String.format(FORMAT, indent, "Koopman [binary]:", koopmanPolyAsBits));
            buffer.append(String.format(FORMAT, indent, "Koopman [hex]:", new BigInteger(koopmanPolyAsBits, 2).toString(16)));

            buffer.append(String.format("%n%sReciprocal poly (similar error detection strength):%n", indent));
            buffer.append(String.format(FORMAT, indent, "mathematical:", CrcUtils.polyAsMathExpression(crc.getWidth(), reciprocalPolyAsBits)));
            buffer.append(String.format(FORMAT, indent, "normal [binary]:", reciprocalPolyAsBits));
            buffer.append(String.format(FORMAT, indent, "normal [hex]:", new BigInteger(reciprocalPolyAsBits, 2).toString(16)));
        }


        if (checksum.isActualAlternateImplementationUsed()) {
            buffer.append(String.format("%n%sspeed:%n", indent));
            buffer.append(String.format(FORMAT, indent, "relative rank:", "unknown, speed is calculated for primary algorithms only"));
        } else {
            int weight = HashAlgorithm.getWeight(checksum.getName());
            if (weight > 1) {
                int rank = HashAlgorithm.getRank(checksum.getName());
                buffer.append(String.format("%n%sspeed:%n", indent));
                buffer.append(String.format(FORMAT, indent, "relative rank:", rank + "/" + allSupportedAlgorithmsCount));
                // long speedpoints = 100-Math.round((double)(100.0*weight/(double)maxWeight));
                // buffer.append(String.format("%s  %-32s%s\n\n", indent, "speed points (100 max):", speedpoints)); // "*".repeat((int)stars)
            }
        }

        buffer.append(String.format("%n%salternative/secondary implementation:%n", indent));
        buffer.append(String.format(FORMAT, indent, "has been requested:", parameters.isAlternateImplementationWanted()));
        buffer.append(String.format(FORMAT, indent, "is available and would be used:", checksum.isActualAlternateImplementationUsed()));
    }

    private int singleView(StringBuilder buffer) throws ExitException {
        try {
            AbstractChecksum checksum = JacksumAPI.getInstance(parameters);
            buffer.append(String.format("  algorithm:%n"));

            if (checksum instanceof CombinedChecksum) {
                buffer.append(String.format("    %-38s%s%n", "name:", checksum.getName()));
                buffer.append(String.format("    %-38s%d%n%n", "actual combined algorithms:", ((CombinedChecksum) checksum).getAlgorithms().size()));
            } else {
                buffer.append(String.format("    %-38s%s%n", "name:", checksum.getName()));
                buffer.append(String.format("%n"));
            }
            printAlgoInfo(buffer,4, checksum);

            // Help.printHelp("en", checksum.getName());
            algorithms++;
            return ExitCode.OK;
        } catch (NoSuchAlgorithmException nsae) {
            throw new ExitException(nsae.getMessage(), ExitCode.PARAMETER_ERROR);
        }
    }

    private int listView(StringBuilder buffer) throws ExitException {
        AbstractChecksum checksum;
        try {
            checksum = JacksumAPI.getInstance(parameters);

            if (checksum instanceof CombinedChecksum) {
                boolean first = true;
                List<AbstractChecksum> allAlgorithms = ((CombinedChecksum) checksum).getAlgorithms();
                algorithms = allAlgorithms.size();
                for (AbstractChecksum cs : allAlgorithms) {
//                    algorithms++;
                    if (parameters.isInfoMode()) {
                        if (first) {
                            buffer.append(String.format("%s:%n", cs.getName()));
                            first = false;
                        } else {
                            buffer.append(String.format("%n%s:%n", cs.getName()));
                        }
                        printAlgoInfo(buffer, 4, cs);
                    } else {
                        buffer.append(String.format("%s%n", cs.getName()));
                    }
                }
            } else {
                return singleView(buffer);
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.PARAMETER_ERROR);
        }
        return 0;
    }

    @Override
    public int perform() throws ExitException {
        StringBuilder buffer = new StringBuilder();
        int exitCode = perform(buffer);
        System.out.print(buffer);
        return exitCode;
    }

    public int perform(StringBuilder buffer) throws ExitException {

        allSupportedAlgorithmsCount = JacksumAPI.getAvailableAlgorithms().size();
        int exitCode = parameters.isList() ? listView(buffer) : singleView(buffer);

        if (parameters.getVerbose().isSummary()) {
            statistics.setAlgorithmCount(algorithms);
            statistics.print(buffer);
        }
        return exitCode;
    }

}
