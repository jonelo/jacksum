/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.cli;

/**
 * This class defines exit codes.
 */
public class ExitCode {

    /** Everything is OK. */
    public final static int OK = 0;
    /** The expectation has been met. */
    public final static int EXPECTATION_MET = 0;

    /** No algorithm has been found ({@code -a unknown:<width>}). */
    public final static int NO_ALGO_FOUND = 1;
    /** At least one file did not match during the verification process (-c). */
    public final static int CHECK_MISMATCH = 1;
    /** Nothing has been found, e.g. the search of an exact help was unsuccessful. */
    public final static int NOTHING_FOUND = 1;
    /** A parameter error. */
    public final static int PARAMETER_ERROR = 2;
    /** The check list resp. the wanted list could not be parsed. */
    public final static int CHECKFILE_PARSE_ERROR = 3;
    /** An I/O error, e.g. a file cannot be read or a character set is not supported. */
    public final static int IO_ERROR = 4;
    /** No file with a wanted hash value has been found (-w). */
    public final static int WANTED_NOTFOUND = 5;
    /** An internal error. */
    public final static int INTERNAL_ERROR = 99;

    /** The expectation has not been met (-e together with file parameters, or --check-strict). */
    public final static int EXPECTATION_NOT_MET = 6;

    /**
     * Creates a new ExitCode instance.
     */
    public ExitCode() {
    }
}
