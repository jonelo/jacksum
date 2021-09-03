/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.multicore.manyfiles;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.jacksum.multicore.manyfiles.Message.Type;
import net.jacksum.parameters.combined.GatheringParameters;
import net.jacksum.parameters.combined.ProducerConsumerParameters;
import net.jacksum.parameters.base.CustomizedFormatParameters;

public class MessageWorker implements Runnable {

    private final int cores;
    private final AlgorithmPool algorithmPool;
    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<Message> outputQueue;
    private final CustomizedFormatParameters formatParameters;
    private final GatheringParameters gatheringParameters;

    public MessageWorker(ProducerConsumerParameters parameters, int cores, AlgorithmPool algorithmPool, BlockingQueue<Message> inputQueue, BlockingQueue<Message> outputQueue) {
        this.cores = cores;
        this.algorithmPool = algorithmPool;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.formatParameters = parameters;
        this.gatheringParameters = parameters;
    }

    @Override
    public void run() {
        //System.out.println("File Consumer started.");        

        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        try {
            Message message;
            // consuming messages until the exit message is received
            while ((message = inputQueue.take()).getType() != Type.EXIT) {
                if (message.getType() == null) {
                    outputQueue.put(message);
                } else switch (message.getType()) {
                    case HASH_FILE:
                    case HASH_STDIN:
                        Runnable worker = new WorkerThread(message, formatParameters, algorithmPool, outputQueue, gatheringParameters);
                        executorService.execute(worker);
                        break;
                    case DONT_HASH_FILE:
                    case DONT_HASH_STDIN:
                        message.setType(Message.Type.FILE_NOT_HASHED);
                        outputQueue.put(message);
                        break;
                    default:
                        outputQueue.put(message);
                        break;
                }
            }
            executorService.shutdown();
            while (!executorService.isTerminated()) {
            }
            outputQueue.put(new Message(Type.EXIT));
            //logQueue.put(new Message(Type.EXIT));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //System.out.println("File Consumer stopped.");
    }
}
