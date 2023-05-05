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

package net.jacksum.actions.io.strings;

import net.jacksum.actions.io.hash.Header;
import net.jacksum.cli.CLIParameters;
import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.io.BOM;
import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.actions.Actions;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.cli.ExitCode;
import net.jacksum.parameters.ParameterException;

import java.io.*;
import java.nio.charset.Charset;

public class HashStringsAction implements Action {

    private final HashStringsActionStatistics statistics;
    private final HashStringsActionParameters parameters;

    public HashStringsAction(HashStringsActionParameters parameters) {
        this.parameters = parameters;
        statistics = new HashStringsActionStatistics();
    }

    @Override
    public int perform() throws
            ParameterException, ExitException {

        if (parameters.isHeaderWanted()) {
            new Header(parameters).print();
        }

        int exitCode = ExitCode.IO_ERROR;
        // the sequence parameter is required
        if (!parameters.isStringList()) {
            throw new ParameterException(String.format("Option %s is required.", CLIParameters.__STRING_LIST));
        }

        AbstractChecksum checksum = Actions.getChecksumInstance(parameters);

        String filename = parameters.getStringList();

        boolean stdin = filename.equals("-");

        BufferedReader bufferedReader = null;
        //FileReader fileReader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        Charset charset = Charset.forName(parameters.getCharsetStringList());

        try {
            // don't use try-with-resources here, because we only want to close the BufferedReader (and FileReader),
            // but we don't want to close System.in
            if (stdin) {
                inputStreamReader = new InputStreamReader(System.in, charset);
            } else {
                fileInputStream = new FileInputStream(filename);
                inputStreamReader = new InputStreamReader(fileInputStream, charset);
            }
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            long lineNumber = 0;
            long processedLines = 0;
            long ignoredLines = 0;
            long emptyLines = 0;

            while ((line = bufferedReader.readLine()) != null) {

                lineNumber++;

                if (lineNumber == 1) {
                    line = BOM.cutBOM(line, charset);
                }

                if (parameters.isIgnoreEmptyLines() && line.length() == 0) {
                    emptyLines++;
                } else
                if (parameters.getCommentChars() != null && line.startsWith(parameters.getCommentChars())) {
                    ignoredLines++;
                } else {

                    // calc checksum from the line
                    checksum.update(line.getBytes());
                    statistics.addBytes(checksum.getLength());
                    parameters.setSequence(Parameters.SequenceType.TXT, line);
                    checksum.setParameters(parameters);
                    // set the line as the filename
                    checksum.setFilename(line);
                    // print out checksum
                    Actions.printChecksum(checksum, parameters);
                    processedLines++;

                    checksum.reset();
                }
            }
            exitCode = ExitCode.OK;

            getStatistics().setTotalLines(lineNumber);
            getStatistics().setHashedLines(processedLines);
            getStatistics().setIgnoredLines(ignoredLines);
            getStatistics().setEmptyLines(emptyLines);

        } catch (FileNotFoundException e) {
            exitCode = ExitCode.PARAMETER_ERROR;
            throw new ParameterException(e.toString());
        } catch (UnsupportedEncodingException e) {
            exitCode = ExitCode.PARAMETER_ERROR;
            throw new ParameterException(e.toString());
        } catch (IOException e) {
            exitCode = ExitCode.IO_ERROR;
            throw new ExitException(e.toString());
        } finally {
            if (!stdin) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        throw new ExitException(e.toString());
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        throw new ExitException(e.toString());
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        throw new ExitException(e.toString());
                    }
                }

            }
        }

        Actions.printStatistics(statistics, parameters);
        return exitCode;
    }

    /**
     * @return the statistics
     */
    public HashStringsActionStatistics getStatistics() {
        return statistics;
    }
}

