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
package net.jacksum.actions.wanted;

import net.jacksum.actions.hashfiles.MessageConsumerStatisticsForHashedFiles;
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

    long filesRead, bytesRead, files_matches_expectation, errors;

    private Parameters parameters;
    private Statistics statistics;
    private List<HashEntry> hashEntries;
    private Map<String, HashEntry> map;
    private Messenger messenger;
    private long found = 0;
    private long notfound = 0;
    WantedListFilter filter = null;

    public MessageConsumerForWantedFiles() {
        statistics = new MessageConsumerStatisticsForHashedFiles();
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
            map.put(hashEntry.getHash(), hashEntry);
        });
        filter = parameters.getWantedListFilter();
    }


    @Override
    public void handleMessage(Message message) {
        
        switch (message.getType()) {
            case FILE_HASHED:
                 filesRead++;
                 bytesRead += message.getPayload().getSize();

                 String hash = FingerprintFormatter.encodeBytes(message.getPayload().getDigest(), parameters.getEncoding(), 0, ' ');
                 if (map.containsKey(hash)) {
                     found++;
                     print(filter.isFilterMatch(), "MATCH", message.getPayload().getPath().normalize().toString(), map.get(hash).getFilename());
                 } else {
                     notfound++;
                     print(filter.isFilterNoMatch(), "NO MATCH", message.getPayload().getPath().normalize().toString(), hash);
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

    @Override
    public void handleMessagesFinal() {

    }

    @Override
    public Statistics getStatistics() {
        ((MessageConsumerStatisticsForHashedFiles)statistics).setFilesRead(filesRead);
        ((MessageConsumerStatisticsForHashedFiles)statistics).setErrors(errors);
        ((MessageConsumerStatisticsForHashedFiles)statistics).setBytesRead(bytesRead);
        if (parameters.isWantedList()) {
            ((MessageConsumerStatisticsForHashedFiles)statistics).setFilesMatchesWanted(found);
            ((MessageConsumerStatisticsForHashedFiles)statistics).setFilesNoMatchesWanted(notfound);
        }
        return statistics;
    }

    @Override
    public int getExitCode() {
        if (parameters.isWantedList()) {
            return found > 0 ? ExitCode.OK: ExitCode.WANTED_NOTFOUND;
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
                System.out.printf("%9s  %s (%s)%s", status, filename, comment, parameters.getLineSeparator());
            }
        }
    }

}
