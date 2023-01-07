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

package net.jacksum.algorithms.primitive;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

import java.io.File;

public class None extends AbstractChecksum {

    public None() {
        super();
        formatPreferences.setHashEncoding(Encoding.HEX);      
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
    }

    @Override
    public void reset() {
        length = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        this.length += length;
    }


    @Override
    public String getValueFormatted() {
        return "";
    }

    @Override
    public long readFile(String filename, boolean reset) {
        this.filename = filename;
        if (isTimestampWanted()) {
            setTimestamp(filename);
        }

        File f = new File(filename);
        length = f.length();
        return length;
    }

    @Override
    public byte[] getByteArray() {
        //return new byte[]{};
        return null;
    }

}
