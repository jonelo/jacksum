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
package net.jacksum.multicore.manyfiles;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import net.jacksum.parameters.combined.ProducerConsumerParameters;

public class Engine {

    private final MessageProducer fileProducer;
    private final MessageWorker fileConsumer;
    private final MessageConsumer outputConsumer;
    
    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<Message> outputQueue;
    
    private final ProducerConsumerParameters parameters;

    private final static int MAX_CORES;
    static {
        MAX_CORES = Runtime.getRuntime().availableProcessors();
    }
    
    public Engine(ProducerConsumerParameters parameters, MessageConsumer consumer)
            throws NoSuchAlgorithmException {
        this.parameters = parameters;
                
        AlgorithmPool algoPool = new AlgorithmPool(parameters);
        inputQueue = new ArrayBlockingQueue<>(1024);
        outputQueue = new ArrayBlockingQueue<>(1024);
                
        fileProducer = new MessageProducer(parameters, inputQueue, outputQueue);
        fileConsumer = new MessageWorker(parameters, MAX_CORES, algoPool, inputQueue, outputQueue);
        //outputConsumer = new MessageConsumerStandard(parameters, outputQueue);
        
        outputConsumer = consumer;
        outputConsumer.setQueue(outputQueue);
    }

    public void start() {
        // Starting producer to produce messages for the inputQueue
        new Thread(fileProducer).start();

        // Starting consumer to consume messages from the inputQueue and to put messages to the outputQueue
        new Thread(fileConsumer).start();
        
        // Starting consumer to consume messages from the outputQueue
        Thread outputConsumerThread = new Thread(outputConsumer);
        outputConsumerThread.start();
        
       // new Thread(logConsumer).start();
       // let the outputConsumerThread finish execution before finishing this thread
       try {
           outputConsumerThread.join();
       } catch (InterruptedException e) {
           System.err.println(e);
       }

    }

}
