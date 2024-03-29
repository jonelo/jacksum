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
package net.jacksum.actions.io.hash;

import net.jacksum.cli.ExitCode;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.StatisticsForHashedFiles;
import net.jacksum.statistics.Statistics;

public class MessageConsumerForHashedFiles extends MessageConsumer {

    long filesRead, bytesRead, files_matches_expectation, errors;
    
    private final Parameters parameters;
    private final Statistics statistics;
    
    public MessageConsumerForHashedFiles(Parameters parameters) {
        this.parameters = parameters;
        statistics = new StatisticsForHashedFiles();
    }

    @Override
    public void handleMessage(Message message) {
        
        switch (message.getType()) {
            case FILE_HASHED_AND_MATCHES_EXPECTATION:
                 filesRead++;
                 files_matches_expectation++;
                 bytesRead += message.getPayload().getSize();
                 System.out.printf("%s%s", message.getInfo(), parameters.getLineSeparator());
                 break;
            case FILE_HASHED:
                 filesRead++;                
                 bytesRead += message.getPayload().getSize();
                 if (!parameters.isExpectation()) {
                     System.out.printf("%s%s", message.getInfo(), parameters.getLineSeparator());
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
        ((StatisticsForHashedFiles)statistics).setFilesRead(filesRead);
        ((StatisticsForHashedFiles)statistics).setErrors(errors);
        ((StatisticsForHashedFiles)statistics).setBytesRead(bytesRead);
        if (parameters.isExpectation()) {
            ((StatisticsForHashedFiles)statistics).setFilesMatchesExpectation(files_matches_expectation);
        }
        return statistics;
    }

    @Override
    public int getExitCode() {
        if (parameters.isExpectation()) {
            return files_matches_expectation > 0 ? ExitCode.OK: ExitCode.CHECK_MISMATCH;
        }
        if (errors > 0) {
            return ExitCode.IO_ERROR;
        }
        return ExitCode.OK;
    }

}
