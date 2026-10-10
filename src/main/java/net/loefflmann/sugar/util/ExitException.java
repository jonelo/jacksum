/*

  Sugar for Java 1.6.0
  Copyright (c) 2001-2026  Dipl.-Inf. (FH) Johann N. Löfflmann,
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

/**
 * This exception is thrown if the program should exit
 */
public class ExitException extends Exception {

    /** The exit code the program should exit with. */
    private final int exitcode;

    /**
     * Creates a new ExitException with a message and the exit code 0.
     *
     * @param s the detail message
     */
    public ExitException(String s) {
        super(s);
        exitcode = 0;
    }

    /**
     * Creates a new ExitException with a message and an exit code.
     *
     * @param s the detail message
     * @param exitcode the exit code
     */
    public ExitException(String s, int exitcode) {
        super(s);
        this.exitcode = exitcode;
    }
    
    /**
     * Creates a new ExitException with an exit code and no detail message.
     *
     * @param exitcode the exit code
     */
    public ExitException(int exitcode) {
        this(null, exitcode);
    }

    /**
     * Returns the exit code the program should exit with.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitcode;
    }

}
