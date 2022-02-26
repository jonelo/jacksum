/*

  Jacksum 3.2.0 - a checksum utility in Java
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
package net.jacksum.actions.check;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import net.jacksum.actions.hashfiles.Header;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.compats.defs.DefaultCompatibilityProperties;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.compats.parsing.InvalidParserParameterException;
import net.jacksum.compats.parsing.Parser;
import org.n16n.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.multicore.manyfiles.Engine;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.cli.ExitCode;

// checking files in the checkfile
public class CheckAction implements Action {

    private final Parameters parameters;
    private MessageConsumerOnCheckedFiles consumer;

    /**
     * Constructor for CheckAction
     *
     * @param parameters for CheckAction
     */
    public CheckAction(Parameters parameters) {
        this.parameters = parameters;
    }

    private CompatibilityProperties buildParserProperties(Parameters parameters) throws ParameterException {
        CompatibilityProperties parserProperties;
        if (parameters.getCompatibilityID() == null) {
            parserProperties = DefaultCompatibilityProperties.getDefaultCompatibilityProperties(parameters);
        } else {
            parserProperties = parameters.getCompatibilityProperties();
        }

        parserProperties.setCheckStrict(parameters.isCheckStrict());
        return parserProperties;
    }

    @Override
    public int perform() throws ParameterException, ExitException {
        if (parameters.isHeaderWanted()) {
            new Header(parameters).print();
        }

        CompatibilityProperties parserProperties = buildParserProperties(parameters);

        Parser parser;
        try {
            parser = new Parser(parserProperties);
        } catch (InvalidParserParameterException pe) {
            throw new ParameterException(pe.getMessage());
        }

        try {
            List<HashEntry> parsedHashEntries = null;

            if (parameters.getCheckFile() != null) {
                    parsedHashEntries = parser.parseFile(parameters.getCheckFile(),
                            Charset.forName(parameters.getCharsetCheckFile()));
            }
            if (parameters.getCheckLine() != null) {

                if (parsedHashEntries == null) {
                    parsedHashEntries = new ArrayList<>();
                }
                HashEntry hashEntry = parser.parseOneLine(parameters.getCheckLine());
                if (hashEntry != null) {
                    parsedHashEntries.add(hashEntry);
                }
            }

            // if the parameter object does not have stored the filenames yet,
            // put the filenames from the check file to the parameter object
            if (parameters.getFilenamesFromCheckFile() == null) {

                // extract the filenames from the parsed file
                List<String> filenamesInCheckFile = new ArrayList<>();
                for (HashEntry hashEntry : parsedHashEntries) {

                    // System.err.printf("DEBUG: hash: >%s< filename: >%s<\n", hashEntry.getHash(), hashEntry.getFilename());
                    if (hashEntry.getFilename().equals(parameters.getStdinName())) {
                        parameters.setStdinForFilenamesFromArgs(true);
                    }
                    filenamesInCheckFile.add(hashEntry.getFilename());
                }
                parameters.setFilenamesFromCheckFile(filenamesInCheckFile);

            }

            consumer = new MessageConsumerOnCheckedFiles(parsedHashEntries);
            consumer.setParameters(parameters);

            try {
                Engine engine = new Engine(parameters, consumer);
                engine.start();
            } catch (NoSuchAlgorithmException nsae) {
                throw new ParameterException(nsae.getMessage());
            }

            
            if (parameters.getVerbose().isSummary()) {               
                // print statistics from the parsing results    
                parser.getStatistics().print();                
                
                // print statistics from the consumer
                consumer.getStatistics().print();
            }
            
            
            // determine the exit code
            if (parameters.isCheckStrict() && parser.getStatistics().getImproperlyFormattedLines() > 0) {
                return ExitCode.CHECKFILE_PARSE_ERROR;
            }
            return consumer.getExitCode();

        } catch (IOException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.IO_ERROR);
        } catch (NotEvenOneEntryFoundException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.CHECKFILE_PARSE_ERROR);
        }

    }

}
