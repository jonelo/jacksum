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
package net.jacksum.actions.info.compat;

import net.jacksum.actions.Action;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.cli.ExitCode;
import net.jacksum.parameters.ParameterException;
import net.loefflmann.sugar.util.ExitException;


public class CompatInfoAction implements Action {

    private final CompatInfoActionParameters parameters;
    
    public CompatInfoAction(CompatInfoActionParameters parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public int perform() throws ParameterException, ExitException {
        CompatibilityProperties props = parameters.getCompatibilityProperties();
        System.out.print(props);       
        //props.getProperties().forEach((key, value) -> System.out.printf("%s=%s\n", key, value));
        return ExitCode.OK;
    }
    
}
