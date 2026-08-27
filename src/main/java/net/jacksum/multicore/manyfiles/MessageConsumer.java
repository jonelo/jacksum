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

import net.jacksum.formats.FormatPreferences;
import net.jacksum.statistics.Statistics;


public abstract class MessageConsumer implements Runnable {
    
    protected ConsumerParameters parameters;
    protected BlockingQueue<Message> queue;
    protected FormatPreferences formatPreferences;

    // the number of messages that could not be consumed, because an unexpected exception occurred
    // while they were being handled, see also run() and getUnexpectedErrors()
    private int unexpectedErrors;

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    public FormatPreferences getFormatPreferences() {
        return formatPreferences;
    }
    
    public void setParameters(ConsumerParameters parameters) {
        this.parameters = parameters;        
    }
    
    public void setQueue(BlockingQueue<Message> queue) {
        this.queue = queue;
    }
    
    public abstract Statistics getStatistics();
    
    /**
     * How to handle the message? It is dependent on its message type.
     * @param message the Message.
     */
    public abstract void handleMessage(Message message);

    public abstract void handleMessagesFinal();
    
    public abstract int getExitCode();

    /**
     * Returns the number of messages that could not be consumed, because an unexpected exception
     * occurred while they were being handled. Implementations of getExitCode() must not report
     * success if that number is greater than zero.
     *
     * @return the number of messages that could not be consumed
     */
    public int getUnexpectedErrors() {
        return unexpectedErrors;
    }

    /**
     * Handles an unexpected exception that occurred while a message was being consumed.
     * Catch everything, not just RuntimeException: an exception that escapes handleMessage()
     * would otherwise kill this thread silently. Engine.start() joins this thread and returns
     * normally, so the action would read the counters as if the job had been finished, all
     * messages that are still on the queue would be lost, and the exit code would signal
     * success although the job has been aborted. See also WorkerThread which catches
     * everything for the same reason.
     *
     * @param throwable the exception that has been thrown while the message was being handled
     */
    private void handleUnexpectedException(Throwable throwable) {
        unexpectedErrors++;
        System.err.printf("Jacksum: Error: %s%n", throwable);
    }

    @Override
    public void run() {
        // System.out.println("Message Consumer started.");
        try {
            Message message;
            // Consuming messages until exit message is received
            while ((message = queue.take()).getType() != Message.Type.EXIT) {
                if (message.getType() != null) {
                    try {
                        handleMessage(message);
                    } catch (Throwable throwable) {
                        handleUnexpectedException(throwable);
                    }
                }
                // logQueue.put(new Message(INFO, "Output Consumer: consumed " + message.getPath()));
            }
            try {
                handleMessagesFinal();
            } catch (Throwable throwable) {
                handleUnexpectedException(throwable);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // System.out.println("Message Consumer stopped.");
    }
}
