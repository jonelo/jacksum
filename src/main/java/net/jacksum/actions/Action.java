/*


  Jacksum 3.7.0 - a checksum utility in Java
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

package net.jacksum.actions;

import net.loefflmann.sugar.util.ExitException;
import net.jacksum.parameters.ParameterException;

/**
 * The interface for an Action. An Action is something that
 * Jacksum can do independently of other actions.
 */
public interface Action {

    /**
     * Performs an action.
     * @return the exit code in case of a successful action.
     * @throws ParameterException if parameters are wrong
     * @throws ExitException if an exit should happen, it makes no sense to continue
     */
    int perform() throws ParameterException, ExitException;
}
