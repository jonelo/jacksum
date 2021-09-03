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

package net.jacksum.actions;

import net.jacksum.statistics.Statistics;
import java.security.NoSuchAlgorithmException;
import net.jacksum.JacksumAPI;
import net.jacksum.actions.infoalgo.AlgoInfoAction;
import net.jacksum.actions.infoapp.AppInfoAction;
import net.jacksum.actions.check.CheckAction;
import net.jacksum.actions.findalgo.FindAlgoAction;
import net.jacksum.actions.hashfiles.HashFilesAction;
import net.jacksum.actions.help.HelpAction;
import net.jacksum.actions.infocompat.CompatInfoAction;
import net.jacksum.actions.quick.QuickAction;
import net.jacksum.actions.version.VersionAction;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.combined.StatisticsParameters;
import net.jacksum.parameters.base.CustomizedFormatParameters;
import net.jacksum.parameters.Parameters;
import net.jacksum.parameters.combined.ChecksumParameters;

public class Actions {

    // no instance, since we only provide static methods here
    private Actions() {}
    
    public static AbstractChecksum getChecksumInstance(ChecksumParameters parameters)
            throws ParameterException {
        AbstractChecksum checksum;
        try {
            checksum = JacksumAPI.getInstance(parameters);
            checksum.setParameters(parameters);
        } catch (NoSuchAlgorithmException e) {
            throw new ParameterException(e.getMessage()
                    + "\nUse -a <code> to specify a valid one."
                    + "\nFor both help and a list of all supported algorithms use -h.");
        }
        return checksum;
    }
    
    public static void printChecksum (AbstractChecksum checksum, CustomizedFormatParameters parameters) {
        if (parameters.isFormatWanted()) {
            System.out.println(checksum.format(parameters.getFormat()));
        } else {
            System.out.println(checksum.toString());
        }
    }

    public static void printStatistics(Statistics statistics, StatisticsParameters parameters) {
        if (parameters.getVerbose().isSummary()) {
            statistics.print();
        }
    }
    
    public static Action getAction(Parameters parameters) {
        switch (parameters.getActionType()) {
            case QUICK:
                return new QuickAction(parameters);
            case FILES:
                return new HashFilesAction(parameters);
            case CHECK:
                return new CheckAction(parameters);
            case FIND_ALGO:
                return new FindAlgoAction(parameters);
            case INFO_ALGO:
                return new AlgoInfoAction(parameters);
            case INFO_APP:
                return new AppInfoAction(parameters);
            case INFO_COMPAT:
                return new CompatInfoAction(parameters);
            case VERSION:
                return new VersionAction(parameters);
            case HELP: // fall through
            default:
                return new HelpAction(parameters);
        }
    }


}
