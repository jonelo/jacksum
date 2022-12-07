/*


  Jacksum 3.4.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
    private int allSupportedAlgorithms;

    int maxWeight = 0;

    public AlgoInfoAction(AlgoInfoActionParameters parameters) {
        this.parameters = parameters;
        this.statistics = new AlgoInfoActionStatistics();
    }

    private static final String FORMAT = "%s  %-38s%s%n";

    private void printAlgoInfo(int indentation, AbstractChecksum checksum) {
        String indent = "  ";
        /*            if (indentation == 0) {
                indent = "";
            } else {
                indent = String.format("%"+indentation+"s", "");
            }*/

        System.out.printf("%shash length:%n", indent);
        System.out.printf(FORMAT, indent, "bits:", checksum.getSize());
        int padOneByte = checksum.getSize() % 8 > 0 ? 1 : 0;
        int bytes = checksum.getSize() / 8 + padOneByte;
        if (padOneByte > 0) {
            System.out.printf(FORMAT, indent, "bits (zero padded to 8-bit bytes):", bytes * 8);
        }
        System.out.printf(FORMAT, indent, "bytes:", bytes);
        System.out.printf(FORMAT, indent, "nibbles:", bytes * 2);

        int blockSize = checksum.getBlockSize();
        if (blockSize > 0) {
            System.out.printf("%n%sblocksize:%n", indent);
            System.out.printf(FORMAT, indent, "bits:", Integer.toString(blockSize * 8));
            System.out.printf(FORMAT, indent, "bytes:", Integer.toString(blockSize));
        }

        if (checksum instanceof CrcInfo) {
            CrcInfo crc = (CrcInfo) checksum;
            byte[] polyAsBytes = crc.getPolyAsBytes();
            String polyAsBits = ByteSequences.formatAsBits(polyAsBytes, crc.getWidth());
            String reversedPolyAsBits = new StringBuilder(polyAsBits).reverse().toString();
            String koopmanPolyAsBits = "1"+polyAsBits.substring(0, polyAsBits.length()-1);
            String reciprocalPolyAsBits = new StringBuilder(koopmanPolyAsBits).reverse().toString();

            System.out.printf("%n%sCRC parameters:%n", indent);
            System.out.printf(FORMAT, indent, "width (in bits):", crc.getWidth());
            System.out.printf(FORMAT, indent, "polynomial [hex]:", new BigInteger(polyAsBits, 2).toString(16));
            System.out.printf(FORMAT, indent, "init [hex]:", ByteSequences.hexformat(crc.getInitialValue(), crc.getWidth() / 4));
            System.out.printf(FORMAT, indent, "refIn:", crc.isRefIn());
            System.out.printf(FORMAT, indent, "refOut:", crc.isRefOut());
            System.out.printf(FORMAT, indent, "xorOut [hex]:", ByteSequences.hexformat(crc.getXorOut(), crc.getWidth() / 4));

            if (checksum instanceof CrcGeneric) {
                CrcGeneric crcGen = (CrcGeneric) checksum;

                // only if we don't overwrite any methods in CrcGeneric we can be sure that the object can be fully constructed again
                // by the value for "Jacksum CRC algo def"
                if (!crcGen.isTainted()) {
                    if (crcGen.getModel().isIncludeLength()) {
                        System.out.printf(FORMAT, indent, "incLenMSO:", crcGen.getModel().isIncludeLengthMSOfirst());
                        if (crcGen.getModel().isXorLength()) {
                            byte[] xorLengthArray = crcGen.getModel().getXorLengthArray();
                            if (xorLengthArray != null) {
                                System.out.printf(FORMAT, indent, "xorLen [hex]:", ByteSequences.format(xorLengthArray));
                            }
                        }
                    }
                    System.out.printf(FORMAT, indent, "Jacksum CRC algo def:", crcGen.getString());
                }
            }

            System.out.printf("%n%sPolynomial representations:%n", indent);
            System.out.printf(FORMAT, indent, "mathematical:", CrcUtils.polyAsMathExpression(crc.getWidth(), polyAsBytes));
            System.out.printf(FORMAT, indent, "normal/MSB first [binary]:", polyAsBits);
            System.out.printf(FORMAT, indent, "normal/MSB first [hex]:", new BigInteger(polyAsBits, 2).toString(16));
            System.out.printf(FORMAT, indent, "reversed/LSB first [binary]:", reversedPolyAsBits);
            System.out.printf(FORMAT, indent, "reversed/LSB first [hex]:", new BigInteger(reversedPolyAsBits, 2).toString(16));
            System.out.printf(FORMAT, indent, "Koopman [binary]:", koopmanPolyAsBits);
            System.out.printf(FORMAT, indent, "Koopman [hex]:", new BigInteger(koopmanPolyAsBits, 2).toString(16));

            System.out.printf("%n%sReciprocal poly (similar error detection strength):%n", indent);
            System.out.printf(FORMAT, indent, "mathematical:", CrcUtils.polyAsMathExpression(crc.getWidth(), reciprocalPolyAsBits));
            System.out.printf(FORMAT, indent, "normal [binary]:", reciprocalPolyAsBits);
            System.out.printf(FORMAT, indent, "normal [hex]:", new BigInteger(reciprocalPolyAsBits, 2).toString(16));
        }


        if (checksum.isActualAlternateImplementationUsed()) {
            System.out.printf("%n%sspeed:%n", indent);
            System.out.printf(FORMAT, indent, "relative rank:", "unknown, speed is calculated for primary algorithms only");
        } else {
            int weight = HashAlgorithm.getWeight(checksum.getName());
            if (weight > 1) {
                int rank = HashAlgorithm.getRank(checksum.getName());
                System.out.printf("%n%sspeed:%n", indent);
                System.out.printf(FORMAT, indent, "relative rank:", rank + "/" + allSupportedAlgorithms);
                // long speedpoints = 100-Math.round((double)(100.0*weight/(double)maxWeight));
                // System.out.printf("%s  %-32s%s\n\n", indent, "speed points (100 max):", speedpoints); // "*".repeat((int)stars)
            }
        }

        System.out.printf("%n%salternative/secondary implementation:%n", indent);
        System.out.printf(FORMAT, indent, "has been requested:", parameters.isAlternateImplementationWanted());
        System.out.printf(FORMAT, indent, "is available and would be used:", checksum.isActualAlternateImplementationUsed());

    }

    private int singleView() throws ExitException {
        try {
            AbstractChecksum checksum = JacksumAPI.getInstance(parameters);
            System.out.printf("  algorithm:%n");

            if (checksum instanceof CombinedChecksum) {
                System.out.printf("    %-38s%s%n", "name:", checksum.getName());
                System.out.printf("    %-38s%d%n%n", "actual combined algorithms:", ((CombinedChecksum) checksum).getAlgorithms().size());
            } else {
                System.out.printf("    %-38s%s%n", "name:", checksum.getName());
                System.out.println();
            }
            printAlgoInfo(4, checksum);

            // Help.printHelp("en", checksum.getName());
            algorithms++;
            return ExitCode.OK;
        } catch (NoSuchAlgorithmException nsae) {
            throw new ExitException(nsae.getMessage(), ExitCode.PARAMETER_ERROR);
        }
    }

    private int listView() throws ExitException {
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
                            System.out.printf("%s:\n", cs.getName());
                            first = false;
                        } else {
                            System.out.printf("\n%s:\n", cs.getName());
                        }
                        printAlgoInfo(4, cs);
                    } else {
                        System.out.printf("%s\n", cs.getName());
                    }
                }
            } else {
                return singleView();
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.PARAMETER_ERROR);
        }
        return 0;
    }

    @Override
    public int perform() throws ExitException {

        allSupportedAlgorithms = JacksumAPI.getAvailableAlgorithms().size();
//        if (parameters.getVerbose().isInfo()) {
//            maxWeight = HashAlgorithm.getMaxWeight();
//        }
        int exitCode;
        if (parameters.isList()) {
            exitCode = listView();
        } else {
            exitCode = singleView();
        }

        if (parameters.getVerbose().isSummary()) {
            statistics.setAlgorithmCount(algorithms);
            statistics.print();
        }
        return exitCode;

    }

}
