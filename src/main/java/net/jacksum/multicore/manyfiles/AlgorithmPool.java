/*


  Jacksum 3.6.0 - a checksum utility in Java
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
package net.jacksum.multicore.manyfiles;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.combined.ChecksumParameters;

public class AlgorithmPool {
    private final Map<Integer,AbstractChecksum> pool;
    private final ChecksumParameters parameters;
    
    private AbstractChecksum newInstance() throws NoSuchAlgorithmException {
        AbstractChecksum checksum = JacksumAPI.getInstance(parameters);
        checksum.setParameters(parameters);        
        return checksum;
    }
        
    public AlgorithmPool(ChecksumParameters parameters) throws NoSuchAlgorithmException {
        this.parameters = parameters;
        pool = new HashMap<>();        
        pool.put(0, newInstance());
    }
    
    synchronized public AbstractChecksum getAlgorithm(int id) throws NoSuchAlgorithmException {
        if (!pool.containsKey(id)) {
            pool.put(id, newInstance());
        }
        return pool.get(id);
    }
}
