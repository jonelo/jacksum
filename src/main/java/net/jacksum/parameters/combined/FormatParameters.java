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
package net.jacksum.parameters.combined;


import net.jacksum.parameters.base.FilenameFormatParameters;
import net.jacksum.parameters.base.LineFormatParameters;
import net.jacksum.parameters.base.TimestampFormatParameters;
import net.jacksum.parameters.base.FingerprintFormatParameters;
import net.jacksum.parameters.base.LengthFormatParameters;

public interface FormatParameters extends
        LineFormatParameters,
        FingerprintFormatParameters,
        FilenameFormatParameters,
        LengthFormatParameters,
        TimestampFormatParameters {    
}
