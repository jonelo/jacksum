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

package net.jacksum.actions.io.quick;

import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.util.ByteSequences;
import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.actions.Actions;
import net.jacksum.actions.io.compare.CompareAndPrintResult;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.cli.ExitCode;
import net.jacksum.parameters.ParameterException;

import java.io.Console;
import java.io.UnsupportedEncodingException;

// quick sequence and quit (no file parameter)
public class QuickAction implements Action {

    private final QuickActionStatistics statisticsQuick;
    private final QuickActionParameters parameters;

    public QuickAction(QuickActionParameters parameters) {
        this.parameters = parameters;
        statisticsQuick = new QuickActionStatistics();
    }

    private char[] readPassword() throws ExitException {
        Console console = System.console();
        if (console == null) {
            throw new ExitException("Console not present.");
        }
        return console.readPassword("Password: ");
    }

    public String readLineFromConsole() throws ExitException {
        Console console = System.console();
        if (console == null) {
            throw new ExitException("Console not present.");
        }
        return console.readLine("Enter a string: ");
    }

    @Override
    public int perform() throws
            ParameterException, ExitException {

        // the sequence parameter is required
        if (!parameters.isSequence()) {
            throw new ParameterException("A sequence has to be set (-q).");
        }

        AbstractChecksum checksum = Actions.getChecksumInstance(parameters);
        if (parameters.getSequenceType().equals(Parameters.SequenceType.PASSWD)) {
            char[] passwd = readPassword();
            if (passwd != null) {
                try {
                    checksum.update(new String(passwd).getBytes(parameters.getCharsetConsole()));
                } catch (UnsupportedEncodingException e) {
                    throw new ParameterException(e.getMessage());
                } finally {
                    java.util.Arrays.fill(passwd, ' ');
                }
            }
            checksum.setFilename("");
        } else
        if (parameters.getSequenceType().equals(Parameters.SequenceType.READLINE)) {
            String line = readLineFromConsole();
            if (line != null) {
                try {
                    checksum.update(line.getBytes(parameters.getCharsetConsole()));
                    parameters.setSequence(Parameters.SequenceType.HEX,
                            ByteSequences.format(line.getBytes(parameters.getCharsetConsole())));
                } catch (UnsupportedEncodingException e) {
                    throw new ParameterException(e.getMessage());
                }
                checksum.setParameters(parameters);
                checksum.setFilename(line);
            }
        } else {
            checksum.setFilename(""); //checksum.setFilename(new String(parameters.getSequenceAsBytes()));
            checksum.update(parameters.getSequenceAsBytes());
        }

        statisticsQuick.addBytes(checksum.getLength());

        int exitCode;
        if (parameters.isExpectation()) {
            CompareAndPrintResult action = new CompareAndPrintResult(checksum, parameters);
            exitCode = action.perform();
        } else {
            Actions.printChecksum(checksum, parameters);
            exitCode = ExitCode.OK;
        }

        Actions.printStatistics(statisticsQuick, parameters);
        return exitCode;
    }
    
    
}
