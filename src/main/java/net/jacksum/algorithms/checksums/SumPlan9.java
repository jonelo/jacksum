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

package net.jacksum.algorithms.checksums;

import java.security.NoSuchAlgorithmException;
import net.jacksum.algorithms.crcs.CrcGeneric;


public class SumPlan9 extends CrcGeneric {

    public SumPlan9() throws NoSuchAlgorithmException{
       super (32, 0x04C11DB7, 0, true, true, 0);      
    }

    
    @Override
    public long getFinal() {
        // backup both length and value to temp variables,
        // so we can launch getValue() multiple times
        long bakLength = length;
        long bakValue = value;
        
        value = super.getFinal();
        byte[] array = { 
	    (byte)((length >> 24) ^ 0xCC),
            (byte)((length >> 16) ^ 0x55),
            (byte)((length >> 8) ^ 0xCC),
            (byte)((length) ^ 0x55)
        };
        update(array);

        long finalValue = value;

        // restore
        length = bakLength;
        value = bakValue;

        return (finalValue & 0xFFFFFFFFL);
    }

}
