/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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

package net.jacksum.actions.info.help;

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
            boolean found = Help.printHelp(parameters.getHelpLanguage(), parameters.getHelpSearchString(), parameters.isExact());
            // an unsuccessful exact search is worth an exit code, because the user has asked
            // for one particular option, algorithm, or section header
            if (!found && parameters.isExact()) {
                return ExitCode.NOTHING_FOUND;
            }
        } else if (parameters.isHelpLanguage()) {
            Help.printHelp(parameters.getHelpLanguage());
        } else {
            Help.printShortHelp();
        }
        return ExitCode.OK;
    }

}
