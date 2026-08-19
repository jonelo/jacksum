/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
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
package net.jacksum.actions.io.hash;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.cli.ExitCode;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.StatisticsForHashedFiles;
import net.jacksum.statistics.Statistics;

import static net.jacksum.cli.CLIParameters._IGNORE_LINES_STARTING_WITH_STRING;

public class MessageConsumerForHashedFiles extends MessageConsumer {

    // the string that lines start with in order to be ignored while they are being read
    // back, if the user has not specified one, see also DefaultCompatibilityProperties
    private final static String DEFAULT_COMMENT_CHARS = "#";

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
                 printLine(message);
                 break;
            case FILE_HASHED:
                 filesRead++;
                 bytesRead += message.getPayload().getSize();
                 if (!parameters.isExpectation()) {
                     printLine(message);
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

    /**
     * Prints the line that has been formatted for a file, and warns if that line
     * cannot be read back.
     *
     * @param message the message that carries the formatted line
     */
    private void printLine(Message message) {
        String line = message.getInfo();

        // A line that starts with the comment string is treated as a comment while it is
        // being read back, so such a file would be skipped silently during a check (-c).
        // That can only happen if the line starts with the file name, which is the case
        // for the styles files-only and sfv, and for the algorithm none.
        String commentChars = parameters.getCommentChars() != null
                ? parameters.getCommentChars()
                : DEFAULT_COMMENT_CHARS;
        if (line.startsWith(commentChars)) {
            System.err.printf("Jacksum: Warning: The line for %s starts with the comment string \"%s\", so this line would be ignored while it is being read back, see also option %s.%n",
                    message.getPayload().getPath() == null ? AbstractChecksum.getStdinName() : message.getPayload().getPath(),
                    commentChars,
                    _IGNORE_LINES_STARTING_WITH_STRING);
        }

        System.out.printf("%s%s", line, parameters.getLineSeparator());
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
        // a message that could not be consumed means that a file has not been processed at all,
        // so this must be checked before any other condition in order not to be masked
        if (getUnexpectedErrors() > 0) {
            return ExitCode.IO_ERROR;
        }
        if (parameters.isExpectation()) {
            return files_matches_expectation > 0 ? ExitCode.OK: ExitCode.CHECK_MISMATCH;
        }
        if (errors > 0) {
            return ExitCode.IO_ERROR;
        }
        return ExitCode.OK;
    }

}
