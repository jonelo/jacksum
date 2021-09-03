/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.actions.version;

import net.jacksum.JacksumAPI;
import org.n16n.sugar.util.ExitException;
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
     * Print program version on standard output.
     */
    public static void printAppVersion() {
        System.out.printf("%s %s\n", JacksumAPI.NAME, JacksumAPI.VERSION);
    }

    public static void printOSIStatement() {
        System.out.printf(
                "This software is OSI Certified Open Source Software.\n"
                + "OSI Certified is a certification mark of the Open Source Initiative.\n");
    }
    
    public static void printShortLicense() {
        System.out.printf(
                "This program is free software: you can redistribute it and/or modify\n"
                + "it under the terms of the GNU General Public License as published by\n"
                + "the Free Software Foundation, either version 3 of the License, or\n"
                + "(at your option) any later version.\n\n"
                        
                + "This program is distributed in the hope that it will be useful,\n"
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                + "GNU General Public License for more details.\n\n"
                        
                + "You should have received a copy of the GNU General Public License\n"
                + "along with this program.  If not, see <https://www.gnu.org/licenses/>.\n");
    }

    public static void printCopyrightHeader() {
        System.out.printf("%s\n<%s>\n", JacksumAPI.COPYRIGHT, JacksumAPI.URI);
    }

    public static void printAppVersionFull() {
        printAppVersion();
        printCopyrightHeader();
        System.out.println();
        printOSIStatement();
        System.out.println();
        printShortLicense();
    }

}
