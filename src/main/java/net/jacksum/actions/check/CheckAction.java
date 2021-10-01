/*

  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

    private CompatibilityProperties buildParserProperties(Parameters parameters) throws ParameterException, ExitException {
        CompatibilityProperties parserProperties;
        if (parameters.getCompatibilityID() == null) {
            parserProperties = new CompatibilityProperties();

            parserProperties.setCompatName("generated-by-parameters");
            parserProperties.setCompatDescription("Parser generated by parameters");
            parserProperties.setHashAlgorithm(parameters.getAlgorithmIdentifier());

            //Encoding.string2Encoding(encoding)
/*            AbstractChecksum checksum;
            try {
                checksum = JacksumAPI.getInstance(parameters);
            } catch(NoSuchAlgorithmException nsae) {
                throw new ParameterException(nsae.getMessage());
            }*/
            if (parameters.getEncoding() != null) {
                parserProperties.setHashEncoding(parameters.getEncoding().toString());
            } else {
                throw new ParameterException("Please specify -E <encoding>.");
            }

            parserProperties.setIgnoreEmptyLines(true);
            if (parameters.getCommentChars() != null) {
                parserProperties.setIgnoreLinesStartingWithString(parameters.getCommentChars());
            } else {
                parserProperties.setIgnoreLinesStartingWithString("#");
            }

            // regular expression
            String regexp;

            // filesize is wanted, because there is a + sign
            if (parameters.getAlgorithmIdentifier().contains("+")) {

                if (parameters.isTimestampWanted()) {
                    // hash, filesize, timestamp, and filename
                    regexp = "^([^ ]+) \\s*(\\d+) \\s*([^ ]+) \\s*[*]*(.*)$";
                    parserProperties.setRegexHashPos(1);
                    parserProperties.setRegexpFilesizePos(2);
                    parserProperties.setRegexpTimestampPos(3);
                    parserProperties.setRegexpFilenamePos(4);
                } else {
                    // hash, filesize, and filename
                    regexp = "^([^ ]+) \\s*(\\d+) \\s*[*]*(.*)$";
                    parserProperties.setRegexHashPos(1);
                    parserProperties.setRegexpFilesizePos(2);
                    parserProperties.setRegexpFilenamePos(3);
                }

            } else {
                if (parameters.isTimestampWanted()) {
                    // hash, timestamp, and filename
                    regexp = "^([^ ]+) \\s*([^ ]+) \\s*[*]*(.*)$";
                    parserProperties.setRegexHashPos(1);
                    parserProperties.setRegexpTimestampPos(2);
                    parserProperties.setRegexpFilenamePos(3);
                } else {
                    // hash, and filename
                    regexp = "^([^ ]+) \\s*[*]*(.*)$";
                    parserProperties.setRegexHashPos(1);
                    parserProperties.setRegexpFilenamePos(2);
                }
            }

            parserProperties.setRegexp(regexp);

// fill props based on values in the parameters object
        } else {
//            try {
                parserProperties = parameters.getCompatibilityProperties();
//                parserProperties = new CompatibilityProperties(parameters.getCompatibilityID());
                // patch the parameters object explicitly, because now the parameters 
                // come from the compatibility object (specified by --compat)
//                parameters.setEncoding(parserProperties.getHashEncoding());
//                parameters.setAlgorithm(parserProperties.getHashAlgorithm());
//            } catch (IOException ex) {
//                throw new ExitException("Jacksum: " + ex.getMessage(), ExitCode.IO_ERROR);
//            }
        }

        parserProperties.setCheckStrict(parameters.isCheckStrict());
        return parserProperties;
    }

    @Override
    public int perform() throws ParameterException, ExitException {

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
