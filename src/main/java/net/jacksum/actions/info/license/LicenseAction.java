/*


  Jacksum 3.5.0 - a checksum utility in Java
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

package net.jacksum.actions.info.license;

import net.jacksum.actions.Action;
import net.jacksum.cli.ExitCode;
import net.loefflmann.sugar.io.GeneralIO;
import net.loefflmann.sugar.util.ExitException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LicenseAction implements Action {

    public LicenseAction() {
    }

    @Override
    public int perform() throws ExitException {
        try {
            List<String> lines = GeneralIO.readLinesFromJarFile("/net/jacksum/legal/license.txt", StandardCharsets.UTF_8);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException ioe) {
            return ExitCode.IO_ERROR;
        }
        return ExitCode.OK;
    }

}