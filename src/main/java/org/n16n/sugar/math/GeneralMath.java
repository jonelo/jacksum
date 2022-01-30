/*

  Sugar for Java 1.6.0
  Copyright (C) 2001-2022  Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package org.n16n.sugar.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GeneralMath {

    public static String decimal2Scientific(String str, int significant) {
        return decimal2Scientific(str, significant, "e");
    }

    public static String decimal2Scientific(String str, int significant, String suffix) {
        StringBuilder sb = new StringBuilder(str);
        boolean signflag = false;

        char sign = sb.charAt(0);
        if ((sign == '-') || (sign == '+')) {
            signflag = true;
            sb.deleteCharAt(0);
        }

        // remove leading zeros
        while ((sb.length() > 0) && (sb.charAt(0) == '0')) {
            sb.deleteCharAt(0);
        }
        if (sb.length() == 0) {
            sb.append('0');
        }

        // remove dot
        // where is the decimal point?
        // or in other words:
        // what's the exponent?
        int point = sb.toString().indexOf("."); // sb.indexOf is 1.4+ only
        if (point == -1) {
            point = sb.length();
        } else {
            sb.deleteCharAt(point);
        }

        while ((sb.length() > 1) && (sb.charAt(0) == '0')) {
            sb.deleteCharAt(0);
            point--;
        }

        // remove tailing zeros
        while ((sb.length() > 1) && (sb.charAt(sb.length() - 1) == '0')) {
            sb.deleteCharAt(sb.length() - 1);
        }

        if (sb.length() == 1 && sb.charAt(0) == '0') {
            // it's just 0, fix the point
            point = 1;
        } else {
            int nks; // Nachkommastellen
            if (significant > 0) {
                while (sb.length() < significant) {
                    sb.append('0');
                }
                while (sb.length() - 1 > significant) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                nks = significant - 1;
            } else {
                nks = sb.length() - 1;
            }

            BigDecimal big = new BigDecimal(sb.toString());
            big = big.movePointLeft(sb.length() - 1);
            big = big.setScale(nks, RoundingMode.HALF_UP);
            sb = new StringBuilder(big.toString());
        }

        // add exponent
        sb.append(suffix);
        sb.append(Integer.toString(point - 1));

        // add sign
        if (signflag) {
            sb.insert(0, sign);
        }
        return sb.toString();
    }

    public static String decimal2Scientific(String str) {
        return decimal2Scientific(str, 0);
    }

    public static String duration(long t) {
        return duration(t, true);
    }

    /**
     * Return a human-readable String which represents a time in ms
     *
     * @param t a time in ms
     * @param ignoreLeadingZeroValues whether leading zero values should be
     * ignored
     * @return a human-readable representation of time as String
     */
    public static String duration(long t, boolean ignoreLeadingZeroValues) {
        StringBuilder sb = new StringBuilder();
        long s = 0;
        long min = 0;
        long h = 0;
        long d = 0;
        long ms = t % 1000;
        if (ignoreLeadingZeroValues) {
            sb.insert(0, ms + " ms");
        }
        t /= 1000;
        if (t > 0) {
            s = t % 60;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, s + " s, ");
            }
            t /= 60;
        }
        if (t > 0) {
            min = t % 60;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, min + " min, ");
            }
            t /= 60;
        }
        if (t > 0) {
            h = t % 24;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, h + " h, ");
            }
            t /= 24;
        }
        if (t > 0) {
            d = t;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, d + " d, ");
            }
        }
        if (ignoreLeadingZeroValues) {
            return sb.toString();
        } else {
            return String.format("%d d, %d h, %d min, %d s, %d ms", d, h, min, s, ms);
        }
    }
    
    
    /**
     * Return a human-readable String which represents a time in ms
     *
     * @param allBytes the number of bytes to be formatted
     * @param ignoreLeadingZeroValues whether leading zero values should be
     * ignored
     * @return a human-readable representation of the number of bytes as a String
     */
    public static String formatByteCountHumanReadable(long allBytes, boolean ignoreLeadingZeroValues) {
        StringBuilder sb = new StringBuilder();
        long KiB = 0;
        long MiB = 0;
        long GiB = 0;
        long TiB = 0;
        long bytes = allBytes % 1024;
        if (ignoreLeadingZeroValues) {
            sb.insert(0, bytes + " bytes");
        }
        allBytes /= 1024;
        if (allBytes > 0) {
            KiB = allBytes % 1024;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, KiB + " KiB, ");
            }
            allBytes /= 1024;
        }
        if (allBytes > 0) {
            MiB = allBytes % 1024;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, MiB + " MiB, ");
            }
            allBytes /= 1024;
        }
        if (allBytes > 0) {
            GiB = allBytes % 1024;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, GiB + " GiB, ");
            }
            allBytes /= 1024;
        }
        if (allBytes > 0) {
            TiB = allBytes;
            if (ignoreLeadingZeroValues) {
                sb.insert(0, TiB + " TiB, ");
            }
        }
        if (ignoreLeadingZeroValues) {
            return sb.toString();
        } else {
            return String.format("%d TiB, %d GiB, %d MiB, %d KiB, %d bytes", TiB, GiB, MiB, KiB, bytes);
        }
    }

}
