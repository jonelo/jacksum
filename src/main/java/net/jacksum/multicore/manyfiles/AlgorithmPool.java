/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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
package net.jacksum.multicore.manyfiles;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.combined.ChecksumParameters;

/**
 * A pool of algorithm instances, one per worker thread, so that several files can be
 * hashed in parallel.
 */
public class AlgorithmPool {
    private final Map<Integer,AbstractChecksum> pool;
    private final ChecksumParameters parameters;
    
    private AbstractChecksum newInstance() throws NoSuchAlgorithmException {
        AbstractChecksum checksum = JacksumAPI.getInstance(parameters);
        checksum.setParameters(parameters);        
        return checksum;
    }
        
    /**
     * Creates a new pool and the instance for the id 0.
     *
     * @param parameters the parameters that determine the algorithm
     * @throws NoSuchAlgorithmException if the algorithm cannot be found
     */
    public AlgorithmPool(ChecksumParameters parameters) throws NoSuchAlgorithmException {
        this.parameters = parameters;
        pool = new HashMap<>();        
        pool.put(0, newInstance());
    }
    
    /**
     * Returns the algorithm instance for the given id, it is created if it does not exist yet.
     *
     * @param id the id, e.g. the id of the worker thread
     * @return the algorithm instance for the id
     * @throws NoSuchAlgorithmException if the algorithm cannot be found
     */
    synchronized public AbstractChecksum getAlgorithm(int id) throws NoSuchAlgorithmException {
        if (!pool.containsKey(id)) {
            pool.put(id, newInstance());
        }
        return pool.get(id);
    }
}
