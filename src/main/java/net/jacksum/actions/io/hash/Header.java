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

package net.jacksum.actions.io.hash;

import net.jacksum.JacksumAPI;
import net.jacksum.parameters.base.HeaderParameters;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Header {

    public static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private final HeaderParameters headerParameters;
    private final String commentChars;

    public Header(HeaderParameters headerParameters) {
        this.headerParameters = headerParameters;
        if (headerParameters.getCommentChars() != null) {
            commentChars = headerParameters.getCommentChars();
        } else {
            commentChars = "#";
        }
    }

    private void printLine(String name, String value) {
        System.out.printf("%s %s: %s%s", commentChars, name, value, headerParameters.getLineSeparator());
        max = Math.max(max, name.length() + value.length() + add);
    }

    private void printEmptyCommentLine() {
        System.out.printf("%s%s", commentChars, headerParameters.getLineSeparator());
    }

    private int max = 0;
    private final static int add = 3; // the blank before the name, the colon, and the blank after the name

    public void print() {
        max = 0;

        // a single, empty comment line
        printEmptyCommentLine();

        // invoked by
        printLine("created by",
                String.format("%s (%s, version: %s)",
                JacksumAPI.getName(),
                JacksumAPI.getURI(),
                JacksumAPI.getVersionString()
                ));

        // invoked on JVM
        printLine("invoked on JVM",
                String.format("%s (vendor: %s, version: %s)",
                        System.getProperty("java.vm.name"),
                        System.getProperty("java.vm.vendor"),
                        System.getProperty("java.vm.version")));

        // invoked on OS
        printLine("invoked on OS",
                String.format("%s (arch: %s, version: %s)",
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version")));

        // invoked on date
        printLine("invoked on date",
                new SimpleDateFormat(FORMAT_ISO8601).format(new Date()));

        // empty comment line
        printEmptyCommentLine();

        // current working directory
        printLine("invoked from",
                System.getProperty("user.dir"));

        // invocation arguments
        printLine("invocation args",
                String.join(" ", headerParameters.getCLIParametersWithQuotes()));

        // a single comment line, filled with underscores
        System.out.printf("%s%s%s", commentChars, "_".repeat(max), headerParameters.getLineSeparator());
    }

}
