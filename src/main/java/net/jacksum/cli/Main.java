/*


  Jacksum 3.4.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

import net.jacksum.actions.Actions;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.Statistics;
import net.loefflmann.sugar.util.ExitException;

/**
 * This is the Jacksum Command Line Interface (CLI).
 */
public class Main {

    /**
     * Creates the Main program (CLI).
     *
     * @param args the program arguments
     * @throws net.loefflmann.sugar.util.ExitException in case an exit should happen.
     */
    public Main(String[] args) throws ExitException {
        Statistics statistics = new StatisticsElapsedTime();

        Parameters parameters;
        int exitCode;
        try {
            parameters = new CLIParameters(args).parse().checked();
            exitCode = Actions.getAction(parameters).perform();
        } catch (ParameterException e) {
            throw new ExitException(e.getMessage() + "\nExit.",
                    ExitCode.PARAMETER_ERROR);
        }
        Actions.printStatistics(statistics, parameters);
        throw new ExitException(exitCode);
    }

    /**
     * Main method for the command line interface (CLI).
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Main main = new Main(args);
        } catch (ExitException e) {
            if (e.getMessage() != null) {
                System.err.println(e.getMessage());
            }
            // we want an exit code explicitly
            System.exit(e.getExitCode());
        }
    }

}
