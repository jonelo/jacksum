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

package net.jacksum.algorithms.checksums;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Fnv1_n extends Fnv0_n {

    private final BigInteger INIT;

    public Fnv1_n(String strBitWidth) throws NoSuchAlgorithmException {
        super(strBitWidth);
        switch (bitWidth) {
            case 32:   INIT = new BigInteger("2166136261");
                       break;
            case 64:   INIT = new BigInteger("14695981039346656037");
                       break;
            case 128:  INIT = new BigInteger("144066263297769815596495629667062367629");
                       break;
            case 256:  INIT = new BigInteger("100029257958052580907070968620625704837092796014241193945225284501741471925557");
                       break;
            case 512:  INIT = new BigInteger("9659303129496669498009435400716310466090418745672637896108374329434462657994582932197716438449813051892206539805784495328239340083876191928701583869517785");
                       break;
            case 1024: INIT = new BigInteger("14197795064947621068722070641403218320880622795441933960878474914617582723252296732303717722150864096521202355549365628174669108571814760471015076148029755969804077320157692458563003215304957150157403644460363550505412711285966361610267868082893823963790439336411086884584107735010676915");
                       break;
            default:   throw new NoSuchAlgorithmException("Unknown algorithm: width " + bitWidth
                           + " is not supported.");
        }
        value = INIT;
    }

    @Override
    public void reset() {
        value = INIT;
        length = 0;
    }

}
