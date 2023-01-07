/*
 *
 * Sugar for Java 1.6.0
 * Copyright (C) 2001-2023  Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, https://johann.loefflmann.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author Johann N. Löfflmann
 *
 */
package net.loefflmann.sugar.util;

public final class GeneralProgram {

    /**
     * Exits if the JVM does not fulfil the requirements, does nothing under a
     * free JVM
     *
     * @param version Java version (e. g. "1.3.1")
     */
    public final static void requiresMinimumJavaVersion(final String version) {
        try {
            String ver = System.getProperty("java.vm.version");
            // no java check under non J2SE-compatible VMs
            if (isJ2SEcompatible() && (ver.compareTo(version) < 0)) {
                System.out.println("ERROR: a newer Java VM is required."
                        + "\nVendor of your Java VM:        " + System.getProperty("java.vm.vendor")
                        + "\nVersion of your Java VM:       " + ver
                        + "\nRequired minimum J2SE version: " + version);

                // let's shut down the entire VM
                // no suitable Java VM has been found
                System.exit(1);
            }
        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }

    public static boolean isSupportFor(String version) {
        return isJ2SEcompatible()
                ? (System.getProperty("java.version").compareTo(version) >= 0)
                : false;
    }

    public static boolean isJ2SEcompatible() {
        String vendor = System.getProperty("java.vm.vendor");
        if ( // gij (http://www.gnu.org/software/classpath)
                vendor.startsWith("Free Software Foundation")
                || // kaffe (http://kaffe.org)
                vendor.startsWith("Kaffe.org")
                || // CACAO (http://www.cacaojvm.org)
                vendor.startsWith("CACAO Team")) {
            return false;
        }
        return true;
    }

}
