/*

 Sugar for Java 1.7.0
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
package net.loefflmann.sugar.io;

import net.loefflmann.sugar.util.ExitException;

/**
 * Reads passwords and lines from the system console.
 */
public class Console {

    /**
     * Creates a new Console.
     */
    public Console() {
    }

    /**
     * Prints a prompt and reads a password from the console with echoing disabled.
     *
     * @param prompt the prompt to print
     * @return the password, or {@code null} if the end of the stream has been reached
     * @throws ExitException if no console is present
     */
    public static char[] readPassword(String prompt) throws ExitException {
        java.io.Console console = System.console();
        if (console == null) {
            throw new ExitException("Console not present.");
        }
        return console.readPassword(prompt);
    }

    /**
     * Prints the prompt {@code "Password: "} and reads a password from the console with echoing disabled.
     *
     * @return the password, or {@code null} if the end of the stream has been reached
     * @throws ExitException if no console is present
     */
    public static char[] readPassword() throws ExitException {
        return readPassword("Password: ");
    }

    /**
     * Prints a prompt and reads a line of text from the console.
     *
     * @param prompt the prompt to print
     * @return the line read, excluding any line-termination characters, or {@code null} if the
     * end of the stream has been reached
     * @throws ExitException if no console is present
     */
    public static String readLine(String prompt) throws ExitException {
        java.io.Console console = System.console();
        if (console == null) {
            throw new ExitException("Console not present.");
        }
        return console.readLine(prompt);
    }

    /**
     * Prints the prompt {@code "Enter a string: "} and reads a line of text from the console.
     *
     * @return the line read, excluding any line-termination characters, or {@code null} if the
     * end of the stream has been reached
     * @throws ExitException if no console is present
     */
    public static String readLine() throws ExitException {
        return readLine("Enter a string: ");
    }

}
