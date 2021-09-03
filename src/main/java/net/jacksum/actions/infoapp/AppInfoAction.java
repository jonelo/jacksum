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
package net.jacksum.actions.infoapp;

import net.jacksum.JacksumAPI;
import net.jacksum.actions.Action;
import net.jacksum.cli.ExitCode;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.CrcMath;
import org.n16n.sugar.util.ExitException;
import org.n16n.sugar.util.Support;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

public class AppInfoAction implements Action {

    private final AppInfoActionParameters parameters;

    public AppInfoAction(AppInfoActionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int perform() throws ExitException {
        printAppVersion();
        printSupportedAlgorithms();
        printSupportedCharsets();
        printSystemProperties();
        printAvailableProcessors();
        return ExitCode.OK;
    }

    private void printAppVersion() {
        System.out.printf("Application name: %s\n\n", JacksumAPI.NAME);
        System.out.printf("Application version: %s\n\n", JacksumAPI.VERSION);
    }

    private void printSupportedAlgorithms() {
        Map<String, String> supportedAlgos = JacksumAPI.getAvailableAlgorithms();

        System.out.println("Supported algorithms (primary ID and description):");
        int keyMaxLength = 0;
        for (String key : supportedAlgos.keySet()) {
            keyMaxLength = Math.max(keyMaxLength, key.length());
        }
        for (String key : supportedAlgos.keySet()) {
            String value = supportedAlgos.get(key);
            System.out.printf("    %-" + (keyMaxLength + 4) + "s%s\n", key, value);
        }

        System.out.println();
        System.out.printf("Supported algorithms: %s\n\n", supportedAlgos.size());

        System.out.println("Default algorithm (primary ID and description):");
        System.out.printf("    %-" + (keyMaxLength + 4) + "s%s\n\n",
                Parameters.ALGORITHM_IDENTIFIER_DEFAULT,
                supportedAlgos.get(Parameters.ALGORITHM_IDENTIFIER_DEFAULT));

        /*           SortedSet<String> sortedKeySet = new TreeSet<>(map.keySet());
             for (String key : sortedKeySet) {
                 String value = map.get(key);
                 System.out.printf("  %-"+(keyMaxLength+4)+"s%s\n", key, value);
             }
         */
        System.out.printf("Supported CRC customizations: > %s\n\n",
                CrcMath.calcSupportedCRCCustomizations());

        System.out.print("Concatenation of different algorithms: unlimited\n\n");

        // System.out.println("Hash Tree supported algorithms: 0");
        // System.out.println("HMAC supported algorithms: 0");
    }

    private void printAvailableProcessors() {
        System.out.printf("Available processors: %s\n\n", Runtime.getRuntime().availableProcessors());
    }

    private static void printSystemProperties() {
        System.out.println("System properties:");
        Map<String, String> map = Support.getSystemPropertiesAsMap();

        SortedSet<String> keys = new TreeSet<>(map.keySet());
        for (String key : keys) {
            if (key.equals("line.separator")) {
                System.out.printf("    %s=%s\n", "line.separator.encoded", map.get(key).replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
            }
            System.out.printf("    %s=%s\n", key, map.get(key));
        }
        System.out.println();

    }

    private static void printSupportedCharsets() {
        System.out.println("Supported character sets (names and aliases):");
        /*        
        Map<String, Charset> m = Charset.availableCharsets();
        for (String s : m.keySet()) {
            System.out.printf("    %s\n", s);
            //Charset cs = m.get(s);
            
            Set<String> aliases = m.get(s).aliases();
            
            if (aliases != null && !aliases.isEmpty()) {
                for (String alias : aliases) {
                    System.out.printf("    aliases: %s, ", alias);
                }
            }
            //System.err.print(", ");
         */

        Map<String, Charset> charsets = Charset.availableCharsets();
        for (String charsetName : charsets.keySet()) {
            // charset name
            System.out.printf("    %s", charsetName);
            Charset charset = charsets.get(charsetName);
            if (!charset.canEncode()) { System.out.print(" (decode only!)"); }
            Iterator aliases = charset.aliases().iterator();
            
            //Show alias
            if (aliases.hasNext()) {
                System.out.print(": ");
            }
            while (aliases.hasNext()) {
                System.out.print(aliases.next());
                if (aliases.hasNext()) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }

        System.out.println();
        System.out.printf("Supported character sets: %s\n\n", charsets.size());
        System.out.printf("Default Charset: %s\n\n", Charset.defaultCharset());
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
