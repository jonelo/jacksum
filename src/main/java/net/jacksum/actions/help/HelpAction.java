/*


  Jacksum 3.4.0 - a checksum utility in Java
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

package net.jacksum.actions.help;

import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.cli.ExitCode;

public class HelpAction implements Action {

    private final HelpActionParameters parameters;

    public HelpAction(HelpActionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int perform() throws ExitException {

        if (parameters.isHelpLanguage() && parameters.isHelpSearchString()) {
            Help.printHelp(parameters.getHelpLanguage(), parameters.getHelpSearchString());
        } else if (parameters.isHelpLanguage()) {
            Help.printHelp(parameters.getHelpLanguage());
        } else {
            Help.printShortHelp();
        }
        return ExitCode.OK;
    }

}
