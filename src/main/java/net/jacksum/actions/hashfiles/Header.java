/*

  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

package net.jacksum.actions.hashfiles;

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

    public void print() {
        String date = new SimpleDateFormat(FORMAT_ISO8601).format(new Date());
        System.out.printf("%s%s", commentChars, headerParameters.getLineSeparator());
        println("invoked by", String.format("%s %s", JacksumAPI.getName(), JacksumAPI.getVersionString()));
        println("invoked on", String.format("OS: %s (arch: %s, version: %s)",
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version")));
        println("invoked on", String.format("JVM: %s (vendor: %s, version: %s)",
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.vendor"),
                System.getProperty("java.vm.version")));
        println("invoked on", date);
        println("invoked from", System.getProperty("user.dir"));
        println("invocation args", String.join(" ", headerParameters.getCLIParameters()));
        System.out.printf("%s%s", commentChars, headerParameters.getLineSeparator());
    }

    public void println(String info, String value) {
        System.out.printf("%s %s: %s%s", commentChars, info, value, headerParameters.getLineSeparator());
    }

}
