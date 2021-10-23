/*

 Sugar for Java 3.0.0
 Copyright (C) 2001-2021  Dipl.-Inf. (FH) Johann N. Löfflmann,
 All Rights Reserved, https://johann.loefflmann.net

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 @author Johann N. Löfflmann

 */
package org.n16n.sugar.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NTFSADSFinder {

    private final static Pattern pattern = Pattern.compile(
        "\\s+" // whitespaces
        + "[0123456789,.]+\\s"   // file size (with possible comma or dot formatting), one whitespace
        + "([^:]+:"    // group 1 = file name, then colon,
        + "[^:]+:"     // then ADS, then colon,
        + ".+)");      // then everything else ($DATA)


    public static ArrayList<String> find(Path path) throws IOException,  InterruptedException {
        // Execute it
        final ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/U", "/C", "dir", "/R", path.toString());
        Process process = builder.start();

        // The correct way to get the output of the command is to start the
        // process, read its output, wait for the process' end and then continue the
        // call flow dependent on the exit value.

        // Read the output of the command
        ArrayList<String> output = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-16LE"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    output.add((matcher.group(1)));
                }
            }
        }
        process.waitFor();
        //System.out.printf("Process ended with exit code %d\n", process.exitValue());
        return output;
    }

}
