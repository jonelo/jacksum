/*


  Jacksum 3.3.0 - a checksum utility in Java
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
package net.jacksum.actions.findalgo.engines;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import net.jacksum.actions.compare.CompareAndFindAlgo;
import net.jacksum.actions.findalgo.FindAlgoEngine;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.crcs.CrcGeneric;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.io.GeneralIO;

/**
 *
 * @author Johann N. Loefflmann
 */
public class FindKnownCRC implements FindAlgoEngine {

    Parameters parameters;
    int searched;
    int found;

    public FindKnownCRC(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void find(int width) throws ParameterException {
        if (width < 8 || width > 64) { // || (width % 8 > 0)) {
            throw new ParameterException("Bit width " + width + " is not supported by the CRC finder.");
        }
        List<String> lines;
        try {
            // read the table
            lines = GeneralIO.readLinesFromJarFile(String.format("/net/jacksum/actions/findalgo/engines/%s", "crc-catalogue.txt"), Charset.forName("UTF-8"), true, "#");
        } catch (IOException ex) {
            throw new ParameterException(String.format("internal error: %s in .jar file not found", "crc-catalogue.txt"));
        }

        List<Entry> entries = new ArrayList<>();
        if (lines != null) {
            // for each entry in the table
            for (String line : lines) {
                String[] entry = line.split(";");
                int w = Integer.valueOf(entry[1].substring(4, entry[1].indexOf(',')));
                if (w >= 8 && w <= 64 && width == w) {
                    entries.add(new Entry(entry[1], entry[0]));
                }

            }
        }

        if (parameters.getVerbose().isInfo()) {
            System.err.printf("Trying %s CRC algorithms with a width of %s bits by testing against well known CRCs ...\n", entries.size(), width);
        }

        
        for (Entry entry : entries) {
            AbstractChecksum checksum;
            try {
                checksum = new CrcGeneric(entry.getId());
                checksum.setParameters(parameters);
                checksum.update(parameters.getSequenceAsBytes());

                CompareAndFindAlgo action = new CompareAndFindAlgo(checksum, parameters);
                action.perform();

                if (parameters.getVerbose().isInfo() && action.getPositives() > 0) {
                    System.err.println("    --> " + entry.getDescription());
                }

                found += action.getPositives();
                searched++;
                // for the Garbage Collector
                checksum = null;
            } catch (NoSuchAlgorithmException e) {
                throw new ParameterException(e.getMessage());
            }

        }

    }

    class Entry {

        public Entry(String id, String description) {
            this.id = id;
            this.description = description;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }
        private String id;
        private String description;

    }

    @Override
    public BigInteger getSearched() {
        return BigInteger.valueOf(searched);
    }

    @Override
    public long getFound() {
        return found;
    }

}
