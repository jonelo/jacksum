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
package net.jacksum.actions.quick;

import net.jacksum.actions.compare.CompareActionInterface;
import net.jacksum.parameters.combined.ChecksumParameters;
import net.jacksum.parameters.base.CustomizedFormatParameters;
import net.jacksum.parameters.base.ExpectationParameters;
import net.jacksum.parameters.base.SequenceParameters;
import net.jacksum.parameters.combined.StatisticsParameters;

/**
 * The parameters for the Quick Action.
 */
public interface QuickActionParameters extends
        ChecksumParameters,
        CompareActionInterface,
        StatisticsParameters,
        CustomizedFormatParameters,
        ExpectationParameters,
        SequenceParameters {
}
