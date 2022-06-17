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

package net.loefflmann.sugar.util;

import java.util.StringTokenizer;


public class Version implements Comparable {
    private int
            major,
            sub,
            minor;

    public Version(int major, int sub) {
        this(major, sub, 0);
    }

    public Version(int major, int sub, int minor) {
        this.major = major;
        this.sub = sub;
        this.minor = minor;
    }

    public Version(String version) {
        major = 0;
        sub = 0;
        minor = 0;
        StringTokenizer st = new StringTokenizer(version, ".");
        if (st.hasMoreTokens()) major = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens()) sub = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens()) minor = Integer.parseInt(st.nextToken());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(8); // XX.YY.ZZ
        sb.append(major);
        sb.append('.');
        sb.append(sub);
        sb.append('.');
        sb.append(minor);
        return sb.toString();
    }

    public int getMajor() {
        return major;
    }

    public int getSub() {
        return sub;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public int compareTo(Object o) {
        Version v = (Version) o;
        if (this.equals(v)) return 0;

        if (
                (major > v.getMajor()) ||
                        ((major == v.getMajor()) && (sub > v.getSub())) ||
                        ((major == v.getMajor()) && (sub == v.getSub()) && (minor > v.getMinor()))
        ) return 1;
        else return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version)) return false;
        Version v = (Version) o;
        return ((major == v.getMajor()) && (sub == v.getSub()) && (minor == v.getMinor()));
    }

    @Override
    public int hashCode() {
        return major * 10000 + sub * 100 + minor;
    }

}
