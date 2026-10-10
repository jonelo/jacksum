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

package net.jacksum.actions.io.compare;

import net.jacksum.algorithms.AbstractChecksum;

/**
 * A compare action that prints the name of the algorithm if the comparison
 * was successful, and counts positive and negative results.
 */
public class CompareAndFindAlgo extends CompareAction {

   /**
    * Creates a new CompareAndFindAlgo.
    *
    * @param checksum the algorithm whose name is printed on a match
    * @param parameters the compare parameters
    */
   public CompareAndFindAlgo
           (AbstractChecksum checksum, CompareActionInterface parameters) {
       this.checksum = checksum;
       this.parameters = parameters;
   }

   @Override
   public void perform(boolean equals) {
       if (equals) {
           positives++;
           System.out.println(checksum.getName());
       } else {
           negatives++;
       }        
   }


}
