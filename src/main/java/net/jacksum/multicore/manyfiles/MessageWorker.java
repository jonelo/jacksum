/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
package net.jacksum.multicore.manyfiles;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jacksum.multicore.manyfiles.Message.Type;
import net.jacksum.parameters.combined.GatheringParameters;
import net.jacksum.parameters.combined.ProducerConsumerParameters;
import net.jacksum.parameters.base.CustomizedFormatParameters;

public class MessageWorker implements Runnable {

    // the largest work queue that is allocated, whatever number of threads is requested;
    // without that cap the multiplication below would overflow the int range, and a
    // negative capacity would be rejected by the queue
    private static final int MAX_QUEUE_CAPACITY = 1024 * 1024;

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

    /**
     * Executes the main message processing loop for handling file hashing tasks.
     * <p>
     * Creates a thread pool with a bounded queue to process incoming messages from the input queue.
     * The thread pool uses a caller-runs policy for rejection handling to implement back pressure.
     * <p>
     * Processing flow:
     * - Continuously consumes messages from the input queue until an EXIT message is received
     * - For HASH_FILE and HASH_STDIN messages: submits a WorkerThread task to the executor service
     * - For DONT_HASH_FILE and DONT_HASH_STDIN messages: marks them as FILE_NOT_HASHED and forwards to output queue
     * - For other message types: forwards them directly to the output queue
     * - Null type messages are forwarded immediately to the output queue
     * <p>
     * After processing all messages, gracefully shuts down the executor service and waits for
     * all worker threads to complete before sending a final EXIT message to the output queue.
     */
    @Override
    public void run() {
        //System.out.println("File Consumer started.");        

        boolean interrupted = false;
        // The thread pool is set up inside the try block, because everything that could
        // stop this thread before the finally block is reached would keep the EXIT
        // message from ever being sent, and the consumer downstream would wait for it
        // forever, see the finally block below.
        ExecutorService executorService = null;
        try {
            // a number of threads that the parameters cannot deliver could still reach
            // this class through the API, and a pool of 0 threads would be rejected
            int threads = Math.max(1, cores);
            // potential fix for issue #30
            int capacity = (int) Math.min((long) threads * 100L, MAX_QUEUE_CAPACITY); // or a memory-based calculation
            executorService = new ThreadPoolExecutor(
                threads, threads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(capacity)
            );
            /*
            ExecutorService executorService = Executors.newFixedThreadPool(cores);
            // The producer thread will be employed to run the task it just submitted. This is effective back pressure.
            // If the caller is running the task itself, it can't produce another tasks until it is done with its current task.
            */
            ((ThreadPoolExecutor)executorService).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

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
        } catch (InterruptedException e) {
            interrupted = true;
            e.printStackTrace();
        } finally {
            // Wait until all submitted WorkerThreads have finished. Block on
            // awaitTermination instead of spinning on isTerminated() (the old
            // busy-wait pinned a full CPU core).
            if (executorService != null) {
                executorService.shutdown();
                while (true) {
                    try {
                        if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        interrupted = true;
                        executorService.shutdownNow();
                        break;
                    }
                }
            }
            // Always send the EXIT poison pill downstream, even on the exceptional
            // path, otherwise MessageConsumer would block forever on take() and
            // Engine.start()'s join() would hang the whole process. Retry through
            // interrupts: the consumer only ever blocks on take(), so put() here
            // cannot deadlock.
            boolean delivered = false;
            while (!delivered) {
                try {
                    outputQueue.put(new Message(Type.EXIT));
                    delivered = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        //System.out.println("File Consumer stopped.");
    }
}
