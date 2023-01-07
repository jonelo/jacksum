/*


  Jacksum 3.5.0 - a checksum utility in Java
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
package net.jacksum.cli;

/**
 * This class defines exit codes.
 */
public class ExitCode {

    public final static int
            OK = 0,
            EXPECTATION_MET = 0,

            NO_ALGO_FOUND = 1,
            CHECK_MISMATCH = 1,
            PARAMETER_ERROR = 2,
            CHECKFILE_PARSE_ERROR = 3,
            IO_ERROR = 4,
            WANTED_NOTFOUND = 5,
            INTERNAL_ERROR = 99,

            EXPECTATION_NOT_MET = 6;
}
