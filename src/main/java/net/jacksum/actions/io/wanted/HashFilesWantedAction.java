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

package net.jacksum.actions.io.wanted;

import net.jacksum.actions.Action;
import net.jacksum.actions.io.hash.Header;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.multicore.manyfiles.Engine;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.util.ExitException;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class HashFilesWantedAction implements Action {

    private final Parameters parameters;
    private MessageConsumer consumer;

    public HashFilesWantedAction(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int perform() throws ExitException, ParameterException {
        if (parameters.isHeaderWanted()) {
            new Header(parameters).print();
        }

        List<HashEntry> hashEntries = null;

        WantedHashes wantedHashes = null;
        if (parameters.isWantedList()) {
            wantedHashes = new WantedHashes(parameters);
            wantedHashes.parse();
            hashEntries = wantedHashes.getParsedHashEntries();
        }

        // treat the -e parameter (expectation) as would it be an entry in the wanted hashes
        if (parameters.isExpectation()) {
            HashEntry hashEntry = new HashEntry();
            hashEntry.setHash(parameters.getExpectedString());
            hashEntry.setFilename(parameters.getExpectedString());
            if (hashEntries == null) {
                hashEntries = new ArrayList<>();
            }
            hashEntries.add(hashEntry);
        }

        consumer = new MessageConsumerForWantedFiles(parameters, hashEntries);

        try {
            Engine engine = new Engine(parameters, consumer);
            engine.start();
        } catch (NoSuchAlgorithmException nsae) {
            throw new ParameterException(nsae.getMessage());
        }

        if (parameters.getVerbose().isSummary()) {
            // print statistics from the parsing results
            if (wantedHashes != null) {
                wantedHashes.getParser().getStatistics().print();
            }

            // print statistics from the consumer
            consumer.getStatistics().print();
        }
        return consumer.getExitCode();
    }


}