/*


  Jacksum 3.5.0 - a checksum utility in Java
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

package net.jacksum.actions.io.hash;

import net.jacksum.multicore.manyfiles.Engine;
import java.security.NoSuchAlgorithmException;
import net.loefflmann.sugar.util.ExitException;
import net.jacksum.actions.Action;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.multicore.manyfiles.MessageConsumer;

public class HashFilesAction implements Action {

    private final Parameters parameters;
    private final MessageConsumer consumer;
    
    public HashFilesAction(Parameters parameters) {
        this.parameters = parameters;
        this.consumer = new MessageConsumerForHashedFiles(parameters);
    }

    @Override
    public int perform() throws ExitException, ParameterException {
        if (parameters.isHeaderWanted()) {
            new Header(parameters).print();
        }

        try {
            Engine engine = new Engine(parameters, consumer);
            engine.start();
        } catch (NoSuchAlgorithmException nsae) {
            throw new ParameterException(nsae.getMessage());
        }
        
        if (parameters.getVerbose().isSummary()) {
            consumer.getStatistics().print();
        }
        return consumer.getExitCode();
    }


}