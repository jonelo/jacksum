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
package net.jacksum.parameters.base;

/**
 * The Parameters for the algorithm
 */
public interface AlgorithmParameters {

     /**
      * Returns the identifier of the algorithm.
      * @return the identifier of the algorithm
      */
     String getAlgorithmIdentifier();

     /**
      * Returns whether an alternate implementation is wanted.
      * An alternate implementation selects a pure Java implementation
      * if available.
      * @return whether an alternate implementation is wanted
      */
     boolean isAlternateImplementationWanted();
     
     
     /*
       Returns wheter also limited algorithms are wanted.
       Limited algorithms are alogs that have contraints for the input.
       Example: The HARAKA-256 and HARAKA-512 algos can only process
       input that has an exact length of 32 bytes - not less and not more
       Those algorithms are disabled by default.
       @return wheter limited algorithmes are wanted
      */
    // public boolean areLimitedAlgorithmsWanted();

}
