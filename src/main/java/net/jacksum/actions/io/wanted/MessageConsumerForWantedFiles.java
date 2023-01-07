/*
 * Jacksum 3.4.0 - a checksum utility in Java
 * Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
package net.jacksum.actions.io.wanted;

import net.jacksum.statistics.StatisticsForHashedFiles;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.formats.FingerprintFormatter;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.Statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConsumerForWantedFiles extends MessageConsumer {

    long filesRead, bytesRead, errors;

    private Parameters parameters;
    private Statistics statistics;
    private List<HashEntry> hashEntries;
    private Map<String, HashEntry> map;
    private Messenger messenger;
    private long found = 0;
    private long notfound = 0;
    MatchFilter filter = null;

    public MessageConsumerForWantedFiles() {
        statistics = new StatisticsForHashedFiles();
        messenger = new Messenger();
    }

    public MessageConsumerForWantedFiles(Parameters parameters, List<HashEntry> list) {
        this();
        this.parameters = parameters;
        this.hashEntries = list;
        this.messenger.setVerbose(parameters.getVerbose());

        // Put the hashEntries to a map for an indexed access by hash
        map = new HashMap<>();
        hashEntries.forEach(hashEntry -> {
          //  System.out.println(hashEntry.getHash());
            map.put(hashEntry.getHash(), hashEntry);
        });
        filter = parameters.getWantedListFilter();
    }


    @Override
    public void handleMessage(Message message) {
        
        switch (message.getType()) {
            case FILE_HASHED:
            case FILE_HASHED_AND_MATCHES_EXPECTATION:
                 filesRead++;
                 bytesRead += message.getPayload().getSize();

                 String hash = FingerprintFormatter.encodeBytes(message.getPayload().getDigest(), formatPreferences.getEncoding(), 0, ' ');
                 String filename = message.getPayload().getPath() == null ? "<stdin>" : message.getPayload().getPath().normalize().toString();

                 if (map.containsKey(hash)) {
                     found++;
                     print(filter.isFilterMatch(), "MATCH", filename, map.get(hash).getFilename());
                 } else {
                     notfound++;
                     print(filter.isFilterNoMatch(), "NO MATCH", filename, hash);
                 }
                 break;
            case ERROR:
                 errors++;
                 System.err.printf("Jacksum: Error: %s%n", message.getInfo());
                 break;
            case INFO:
            case INFO_DIR_IGNORED:
                System.err.printf("Jacksum: Info: %s%n", message.getInfo());
                break;
            default:
                break;
        }
    }

    private int exitCode = 0;
    @Override
    public void handleMessagesFinal() {

        // expectation is met if it matches (or not matches) at least one file
        if (parameters.isExpectation() && filesRead > 0) {
            long checkAgainst = filter.isFilterMatch() ? found : notfound;

            System.err.printf("%sJacksum: Expectation %s.%s",
                    parameters.getLineSeparator(),
                    checkAgainst > 0 ? "met" : "not met",
                    parameters.getLineSeparator());
            exitCode = checkAgainst > 0 ? ExitCode.EXPECTATION_MET : ExitCode.EXPECTATION_NOT_MET;

            System.err.printf("Jacksum: %d of the successfully read files %s the expected hash value.%s",
                    checkAgainst,
                    filter.isFilterMatch() ?  (checkAgainst == 1 ? "matches": "match") : (checkAgainst == 1 ? "doesn't match" : "don't match"),
                    parameters.getLineSeparator());
        }

        if (parameters.isWantedList() && filesRead > 0) {
            long checkAgainst = filter.isFilterMatch() ? found : notfound;
            exitCode = checkAgainst > 0 ? ExitCode.OK: ExitCode.WANTED_NOTFOUND;
        }

    }

    @Override
    public Statistics getStatistics() {
        ((StatisticsForHashedFiles)statistics).setFilesRead(filesRead);
        ((StatisticsForHashedFiles)statistics).setErrors(errors);
        ((StatisticsForHashedFiles)statistics).setBytesRead(bytesRead);
        ((StatisticsForHashedFiles)statistics).setTotalNumberOfWantedHashes(hashEntries.size());
        if (parameters.isWantedList() || parameters.isExpectation()) {
            ((StatisticsForHashedFiles)statistics).setFilesMatchesWanted(found);
            ((StatisticsForHashedFiles)statistics).setFilesNoMatchesWanted(notfound);
        }
        return statistics;
    }

    @Override
    public int getExitCode() {
        if (parameters.isExpectation() || parameters.isWantedList()) {
            return exitCode;
        }
        if (errors > 0) {
            return ExitCode.IO_ERROR;
        }
        return ExitCode.OK;
    }

    private void print(boolean output, String status, String filename, String comment) {
        if (output) {
            if (parameters.isList()) {
                System.out.printf("%s%s", filename, parameters.getLineSeparator());
            } else {
                if (comment != null) {
                    System.out.printf("%9s  %s (%s)%s", status, filename, comment, parameters.getLineSeparator());
                } else { // comment can be null if --style hexhashes-only has been selected
                    System.out.printf("%9s  %s%s", status, filename, parameters.getLineSeparator());
                }
            }
        }
    }

}
