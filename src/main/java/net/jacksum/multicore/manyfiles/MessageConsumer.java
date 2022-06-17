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
package net.jacksum.multicore.manyfiles;

import java.util.concurrent.BlockingQueue;
import net.jacksum.statistics.Statistics;


public abstract class MessageConsumer implements Runnable {
    
    protected ConsumerParameters parameters;
    protected BlockingQueue<Message> queue;
    
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
    
    @Override
    public void run() {
        // System.out.println("Message Consumer started.");
        try {
            Message message;
            // Consuming messages until exit message is received
            while ((message = queue.take()).getType() != Message.Type.EXIT) {
                if (message.getType() != null) {
                    handleMessage(message);
                }
                // logQueue.put(new Message(INFO, "Output Consumer: consumed " + message.getPath()));
            }
            handleMessagesFinal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // System.out.println("Message Consumer stopped.");
    }   
}
