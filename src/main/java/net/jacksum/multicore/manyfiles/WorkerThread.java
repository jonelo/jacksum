/*


  Jacksum 3.6.0 - a checksum utility in Java
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
package net.jacksum.multicore.manyfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.combined.GatheringParameters;
import net.jacksum.parameters.base.CustomizedFormatParameters;

public class WorkerThread implements Runnable {

    private final Message message;
    private final AlgorithmPool algorithmPool;
    private final BlockingQueue<Message> outputQueue;
    private final CustomizedFormatParameters formatParameters;
    private final GatheringParameters gatheringParameters;

    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);
    //private static int value = 0;

    private static class ThreadID extends ThreadLocal<Integer> {

        @Override
        protected Integer initialValue() {
            return nextId.getAndIncrement();
        }

    }

    private static final ThreadID threadID = new ThreadID();

    public WorkerThread(Message message, CustomizedFormatParameters formatParameters, AlgorithmPool algorithmPool, BlockingQueue<Message> outputQueue, GatheringParameters gatheringParameters) {
        this.message = message;
        this.formatParameters = formatParameters;
        this.gatheringParameters = gatheringParameters;
        this.algorithmPool = algorithmPool;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        //System.out.println(Thread.currentThread().getName() + " Start. Payload = " + message.getPath());
        processMessage();
        //System.out.println(Thread.currentThread().getName() + " End.");
    }

    private void processMessage() {

        AbstractChecksum algorithm;
        // construct the message
        try {
            // get the algorithm instance
            algorithm = algorithmPool.getAlgorithm(threadID.get());

            // calculate the digest
            //System.out.println(message.getPayload().getPath());
            if (message.getType().equals(Message.Type.HASH_FILE)) {
                if (message.getPayload().getSpecialPath() != null) {
                    algorithm.readFile(message.getPayload().getSpecialPath(), true);
                } else {
                    algorithm.readFile(message.getPayload().getPath().toString(), true);
                }

            } else if (message.getType().equals(Message.Type.HASH_STDIN)) {
                algorithm.readStdin();
            }

            // set the digest to the payload of the message
            message.getPayload().setDigest(algorithm.getByteArray());

            // set the size to the payload of the message
            message.getPayload().setSize(algorithm.getLength());

            if (message.getType().equals(Message.Type.HASH_FILE)) {
                // set the file attributes to the payload of the message (for regular files only)
                if (gatheringParameters.isTimestampWanted() && message.getPayload().getPath() != null) {
                        BasicFileAttributes attrs = Files.readAttributes(message.getPayload().getPath(), BasicFileAttributes.class);
                        message.getPayload().setBasicFileAttributes(attrs);
                }
            }

            // set the info of the payload
            // message.setInfo(algorithm.toString() + " (" + threadID.get() + ")");            
            if (formatParameters.isFormatWanted()) {
                message.setInfo(algorithm.format(formatParameters.getFormat()));
            } else {
                message.setInfo(algorithm.toString());
            }

            if (gatheringParameters.isExpectation() && algorithm.getValueFormatted().equals(gatheringParameters.getExpectedString())) {
                message.setType(Message.Type.FILE_HASHED_AND_MATCHES_EXPECTATION);
            } else {
                // if the file has been hashed, change the command                
                message.setType(Message.Type.FILE_HASHED);
            }

        } catch (NoSuchAlgorithmException | IOException ex) {
            message.setType(Message.Type.ERROR);
            message.setInfo(ex.getMessage());
            //Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            // put the message to the output queue
            // filter: put only those on the output queue that match the expected string (-e)
//            if (!gatheringParameters.isExpectation() ||
//                gatheringParameters.isExpectation() && message.getType().equals(Message.Type.FILE_HASHED_AND_MATCHES_EXPECTATION)) {
            outputQueue.put(message);
//            }
            //logQueue.put(new Message(INFO, "processing command "+message.getPath().toString()));
        } catch (InterruptedException ex) {
            Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
