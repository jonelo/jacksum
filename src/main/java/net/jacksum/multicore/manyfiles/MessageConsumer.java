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
package net.jacksum.multicore.manyfiles;

import java.util.concurrent.BlockingQueue;

import net.jacksum.formats.FormatPreferences;
import net.jacksum.statistics.Statistics;


/**
 * The base class of a consumer that takes messages from a queue and handles them,
 * until the exit message is received.
 */
public abstract class MessageConsumer implements Runnable {
    
    /** The parameters of the consumer. */
    protected ConsumerParameters parameters;
    /** The queue from which the messages are taken. */
    protected BlockingQueue<Message> queue;
    /** The format preferences. */
    protected FormatPreferences formatPreferences;

    // the number of messages that could not be consumed, because an unexpected exception occurred
    // while they were being handled, see also run() and getUnexpectedErrors()
    private int unexpectedErrors;

    /**
     * Creates a new MessageConsumer.
     */
    public MessageConsumer() {
    }

    /**
     * Sets the format preferences.
     *
     * @param formatPreferences the format preferences
     */
    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    /**
     * Gets the format preferences.
     *
     * @return the format preferences
     */
    public FormatPreferences getFormatPreferences() {
        return formatPreferences;
    }
    
    /**
     * Sets the parameters of the consumer.
     *
     * @param parameters the parameters
     */
    public void setParameters(ConsumerParameters parameters) {
        this.parameters = parameters;        
    }
    
    /**
     * Sets the queue from which the messages are taken.
     *
     * @param queue the queue
     */
    public void setQueue(BlockingQueue<Message> queue) {
        this.queue = queue;
    }
    
    /**
     * Gets the statistics that have been collected while the messages were consumed.
     *
     * @return the statistics
     */
    public abstract Statistics getStatistics();
    
    /**
     * How to handle the message? It is dependent on its message type.
     * @param message the Message.
     */
    public abstract void handleMessage(Message message);

    /**
     * Called once after the exit message has been received, to finish the handling of
     * all messages.
     */
    public abstract void handleMessagesFinal();
    
    /**
     * Gets the exit code that results from the messages that have been consumed.
     *
     * @return the exit code
     */
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

    /**
     * Takes messages from the queue and handles them until the exit message is received,
     * then calls {@link #handleMessagesFinal()}. Unexpected exceptions are counted, see
     * {@link #getUnexpectedErrors()}.
     */
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
