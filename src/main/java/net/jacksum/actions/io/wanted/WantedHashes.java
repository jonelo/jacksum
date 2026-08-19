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
package net.jacksum.actions.io.wanted;

import net.jacksum.actions.io.verify.NotEvenOneEntryFoundException;
import net.jacksum.cli.ExitCode;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.compats.defs.DefaultCompatibilityProperties;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.compats.parsing.InvalidParserParameterException;
import net.jacksum.compats.parsing.Parser;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.util.ExitException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class WantedHashes {
    private Parameters parameters;
    private CompatibilityProperties compatibilityProperties;
    private Parser parser;
    List<HashEntry> parsedHashEntries = null;

    public WantedHashes(Parameters parameters) throws ParameterException {
        this.parameters = parameters;

        // get the Parser's properties
        if (parameters.isWantedList()) {
            this.compatibilityProperties = buildParserProperties(parameters);
        } else {
            return;
        }

        // construct the parser by using Parser's properties
        try {
            parser = new Parser(compatibilityProperties);
            parser.setReplaceDuplicateFilenames(false);
        } catch (InvalidParserParameterException pe) {
            throw new ParameterException(pe.getMessage());
        }

    }

    public Parser getParser() {
        return parser;
    }

    // Note: a check line (--check-line) is not taken into account here, because such a line always
    // asks for a verification: getActionType() returns ActionType.CHECK if a check line has been
    // specified, and Parameters.checkParameters() rejects the combination of --check-line and
    // --wanted-list, so this class is never used for a check line at all.
    public void parse() throws ExitException {
        // parse
        try {
            parsedHashEntries = parser.parseFile(parameters.getWantedList(),
                        Charset.forName(parameters.getCharsetWantedList()));
        } catch (IOException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.IO_ERROR);
        } catch (NotEvenOneEntryFoundException ex) {
            throw new ExitException(ex.getMessage(), ExitCode.CHECKFILE_PARSE_ERROR);
        }
    }

    public List<HashEntry> getParsedHashEntries() {
        return parsedHashEntries;
    }

    // -------------------------- private methods ----------------------------------- //
    private CompatibilityProperties buildParserProperties(Parameters parameters) throws ParameterException {
        CompatibilityProperties parserProperties;
        if (parameters.getCompatibilityID() == null) {
            parserProperties = DefaultCompatibilityProperties.getDefaultCompatibilityProperties(parameters);
        } else {
            parserProperties = parameters.getCompatibilityProperties();
        }
        return parserProperties;
    }

}
