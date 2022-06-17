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
package net.jacksum.actions.version;

import net.jacksum.JacksumAPI;
import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.cli.ExitCode;

public class VersionAction implements Action {

    private final VersionActionParameters parameters;

    public VersionAction(VersionActionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int perform() throws ExitException {
        if (parameters.getVerbose().isInfo()) {
            printAppVersionFull();
        } else {
            printAppVersion();
        }
        return ExitCode.OK;
    }

    /**
     * Prints the program version on standard output.
     */
    public static void printAppVersion() {
        System.out.printf("%s %s%n", JacksumAPI.NAME, JacksumAPI.VERSION);
    }

    public static void printAppVersionAndURI() {
        System.out.printf("%s %s, <%s>%n", JacksumAPI.NAME, JacksumAPI.VERSION, JacksumAPI.URI);
    }

    public static void printOSIStatement() {
        System.out.printf(
                "    This software is OSI Certified Open Source Software.%n"
                + "    OSI Certified is a certification mark of the Open Source Initiative.%n");
    }
    
    public static void printShortLicense() {
        System.out.printf(
                  "    This program is free software: you can redistribute it and/or modify it%n"
                + "    under the terms of the GNU General Public License as published by the Free%n"
                + "    Software Foundation, either version 3 of the License, or (at your opinion)%n"
                + "    any later version.%n%n"
                        
                + "    This program is distributed in the hope that it will be useful, but%n"
                + "    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY%n"
                + "    or FITNESS FOR A PARTICULAR PURPOSE. Type `jacksum --license` for details.%n%n");
                        
    }

    public static void printCopyrightHeader() {
        System.out.println(JacksumAPI.COPYRIGHT);
    }

    public static void printAppVersionFull() {
        printAppVersionAndURI();
        printCopyrightHeader();
        System.out.println();
        printOSIStatement();
        System.out.println();
        printShortLicense();
    }

}
