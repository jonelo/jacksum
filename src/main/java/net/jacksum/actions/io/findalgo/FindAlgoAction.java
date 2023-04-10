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
package net.jacksum.actions.io.findalgo;

import net.jacksum.actions.io.findalgo.engines.BruteForceCRC;
import net.jacksum.actions.io.findalgo.engines.FindDocumentedAlgorithms;
import net.jacksum.actions.io.findalgo.engines.FindKnownCRC;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.cli.ExitCode;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.Statistics;

public class FindAlgoAction implements Action {

    private final Parameters parameters;
    private int found;
    private BigInteger searched;
    private final Statistics statistics;

    public FindAlgoAction(Parameters parameters) {
        this.found = 0;
        this.searched = BigInteger.ZERO;
        this.parameters = parameters;
        this.statistics = new FindAlgoActionStatistics();
    }

    @Override
    public int perform() throws ParameterException, ExitException {
        // e.g. unknown:32
        Pattern pattern = Pattern.compile("^unknown:(\\d+)$");
        Matcher matcher = pattern.matcher(parameters.getAlgorithmIdentifier());

        if (matcher.find() && matcher.groupCount() == 1) {
            int width = Integer.valueOf(matcher.group(1));
            /*if (width < 8 || (width % 8 > 0)) {
                throw new ParameterException("Bit width must be a multiple of 8. Selected bit with " + width + " does not match that criteria.");
            }*/

            List<FindAlgoEngine> engines = new ArrayList<>(3);
            engines.add(new FindDocumentedAlgorithms(parameters));
            engines.add(new FindKnownCRC(parameters));
            engines.add(new BruteForceCRC(parameters));
            // engines.add(new BruteForceCRC_Extended(parameters); // extended Rocksoft Model where the size is calculated into the crc

            for (FindAlgoEngine engine : engines) {
                try {
                    engine.find(width);
                    searched = searched.add(engine.getSearched());
                    found += engine.getFound();
                    System.err.println();
                } catch (ParameterException pe) {
                    System.err.println(pe.getMessage());
                }
            }

        } else {
            throw new ParameterException("Invalid algorithm parameter.");
        }

        if (parameters.getVerbose().isSummary()) {
            ((FindAlgoActionStatistics) statistics).setFound(found);
            ((FindAlgoActionStatistics) statistics).setSearched(searched);
            statistics.print();
        }
        if (found == 0) {
            return ExitCode.NO_ALGO_FOUND;
        } else {
            return ExitCode.OK;
        }
    }

}
