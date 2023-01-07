/*


  Jacksum 3.5.0 - a checksum utility in Java
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
package net.jacksum.actions.io.findalgo.engines;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import net.jacksum.JacksumAPI;
import net.jacksum.actions.io.compare.CompareAndFindAlgo;
import net.jacksum.actions.io.findalgo.FindAlgoEngine;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;

/**
 *
 * @author Johann
 */
public class FindDocumentedAlgorithms implements FindAlgoEngine {

    private final Parameters parameters;
    private int searched;
    private int found;

    public FindDocumentedAlgorithms(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void find(int width) throws ParameterException {
        Map<String, String> map = JacksumAPI.getAvailableAlgorithms(width);
        if (parameters.getVerbose().isInfo()) {
            System.err.printf("Trying %s algorithms with a width of %s bits that are supported by %s %s ...\n", map.size(), width, JacksumAPI.NAME, JacksumAPI.VERSION);
        }

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            AbstractChecksum checksum;
            try {
                checksum = JacksumAPI.getChecksumInstance(entry.getKey());
            } catch (NoSuchAlgorithmException e) {
                throw new ParameterException("Error: No such algorithm: " + entry.getKey());
            }
            checksum.setParameters(parameters);
            checksum.update(parameters.getSequenceAsBytes());

            CompareAndFindAlgo action = new CompareAndFindAlgo(checksum, parameters);
            action.perform();
            if (parameters.getVerbose().isInfo() && action.getPositives() > 0) {
                System.err.println("    --> " + entry.getValue());
            }
            found += action.getPositives();
            searched++; // = searched.add(BigInteger.ONE);
            // for the GC
            checksum = null;
        }
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
