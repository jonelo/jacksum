/*


  Jacksum 3.3.0 - a checksum utility in Java
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
package net.jacksum.actions.infoalgo;

import java.security.NoSuchAlgorithmException;
import java.util.List;
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

    private void printAlgoInfo(int indentation, AbstractChecksum checksum) {
        String indent = "  ";
        /*            if (indentation == 0) {
                indent = "";
            } else {
                indent = String.format("%"+indentation+"s", "");
            }*/

        System.out.printf("%shash length:\n", indent);
        System.out.printf("%s  %-32s%s\n", indent, "bits:", checksum.getSize());
        System.out.printf("%s  %-32s%s\n", indent, "bytes:", checksum.getSize() / 8);
        System.out.printf("%s  %-32s%s\n", indent, "nibbles:", checksum.getSize() / 4);
        System.out.println();

        int blockSize = checksum.getBlockSize();
        if (blockSize > 0) {
            System.out.printf("%sblocksize:\n", indent);
            System.out.printf("%s  %-32s%s\n", indent, "bits:", Integer.toString(blockSize * 8));
            System.out.printf("%s  %-32s%s\n", indent, "bytes:", Integer.toString(blockSize));
            System.out.println();
        }

        if (checksum instanceof CrcGeneric) {
            CrcGeneric crc = (CrcGeneric) checksum;
            System.out.printf("%sCRC details:\n", indent);
            System.out.printf("%s  %-32s%s\n", indent, "Parameters:", crc.getString());
            System.out.printf("%s  %-32s%s\n", indent, "Width (in bits):", crc.getWidth());
            System.out.printf("%s  %-32s%s\n", indent, "Polynomial (as hex):", ByteSequences.hexformat(crc.getPoly(), crc.getWidth() / 4));
            System.out.printf("%s  %-32s%s\n", indent, "Polynomial:", CrcGeneric.polyAsMathExpression(crc.getWidth(), crc.getPoly()));
            System.out.printf("%s  %-32s%s\n", indent, "Init:", ByteSequences.hexformat(crc.getInitialValue(), crc.getWidth() / 4));
            System.out.printf("%s  %-32s%s\n", indent, "RefIn:", crc.getRefIn());
            System.out.printf("%s  %-32s%s\n", indent, "RefOut:", crc.getRefOut());
            System.out.printf("%s  %-32s%s\n", indent, "XorOut:", ByteSequences.hexformat(crc.getXorOut(), crc.getWidth() / 4));

            if (crc.isIncludeLength()) {
                System.out.printf("%s  %-30s%s\n", indent, "IncLenMSO:", crc.isIncludeLengthMSOfirst());
                if (crc.isXorLength()) {
                    byte[] xorLengthArray = crc.getXorLengthArray();
                    if (xorLengthArray != null) {
                        System.out.printf("%s  %-30s%s\n", indent, "XorLen:", ByteSequences.format(xorLengthArray));
                    }
                }
            }
            System.out.println();
        }

        int weight = HashAlgorithm.getWeight(checksum.getName());
        if (weight > 1) {
            int rank = HashAlgorithm.getRank(checksum.getName());
            System.out.printf("%sspeed:\n", indent);            
            System.out.printf("%s  %-32s%s\n", indent, "relative rank:", rank + "/" + allSupportedAlgorithms);
           // long speedpoints = 100-Math.round((double)(100.0*weight/(double)maxWeight));
           // System.out.printf("%s  %-32s%s\n\n", indent, "speed points (100 max):", speedpoints); // "*".repeat((int)stars)
        }

        System.out.printf("\n%salternative implementation:\n", indent);
        System.out.printf("%s  %-32s%s\n", indent, "has been requested:", parameters.isAlternateImplementationWanted());
        System.out.printf("%s  %-32s%s\n", indent, "is available and would be used:", checksum.isActualAlternateImplementationUsed());

    }

    private int singleView() throws ExitException {
        try {
            AbstractChecksum checksum = JacksumAPI.getInstance(parameters);
            System.out.print("  algorithm:\n");

            if (checksum instanceof CombinedChecksum) {
                System.out.printf("    %-32s%s\n", "name:", checksum.getName());
                System.out.printf("    %-32s%d\n\n", "actual combined algorithms:", ((CombinedChecksum) checksum).getAlgorithms().size());
            } else {
                System.out.printf("    %-32s%s\n", "name:", checksum.getName());
                System.out.print("\n");
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
