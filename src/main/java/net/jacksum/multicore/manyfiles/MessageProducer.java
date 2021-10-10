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

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import net.jacksum.multicore.manyfiles.Message.Type;

public class MessageProducer implements Runnable {

    private final BlockingQueue<Message> inputQueue;
    private final BlockingQueue<Message> outputQueue;
    private final ProducerParameters producerParameters;
    private final List<String> allFiles;
    private final static boolean onWindows = System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows");

    public MessageProducer(
            ProducerParameters producerParameters,
            BlockingQueue<Message> inputQueue,
            BlockingQueue<Message> outputQueue) {

        this.producerParameters = producerParameters;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;

        //List<String> filenamesFromCheckFile = producerParameters.getFilenamesFromCheckFile();
        // construct all files that needs to be processed
        List<String> merged = new ArrayList<>();
        merged.addAll(producerParameters.getFilenamesFromArgs());
        merged.addAll(producerParameters.getFilenamesFromFilelist());
       /* if (producerParameters.getFilenamesFromCheckFile() != null) {
            merged.addAll(producerParameters.getFilenamesFromCheckFile());
        }*/

        // remove duplicates:
        // construct a new list from the set constructed from elements
        // of the original list
        allFiles = merged.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private void handleFilename(String filename, boolean filenameIsInCheckFile, Message.Type messageTypeForFiles) {
        try {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    if (filenameIsInCheckFile) {
                        outputQueue.put(new Message(Type.ERROR, path, String.format("%s: directory found in check file, but a filename was expected.", path)));
                    } else {
                        FileWalker fileWalker = new FileWalker(
                                messageTypeForFiles,
                                producerParameters,
                                path,
                                inputQueue);
                        fileWalker.walk();
                    }

                } else if (Files.isRegularFile(path)) {
                    inputQueue.put(new Message(messageTypeForFiles, path));
                } else {
                    if (!onWindows && producerParameters.IsReadAllUnixFileTypes()) {
                        inputQueue.put(new Message(messageTypeForFiles, path));
                    } else {
                        // a fifo for example (mkfifo myfifo)
                        outputQueue.put(new Message(Type.ERROR, path, String.format("%s: is not a regular file.", path)));
                    }
                }
            } else {
                outputQueue.put(new Message(Type.ERROR, path, String.format("%s: does not exist.", path)));
            }

        } catch (InvalidPathException e) {
            // shell wasn't able to resolve wildcards and simply passes the wildcard string to the app.
            // the Windows File System Parser cannot transform the string that contains wildcards
            // to a path, and it throws an InvalidPathException
            try {
                outputQueue.put(new Message(Type.ERROR, null, String.format("%s: does not match anything.", filename)));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                // Logger.getLogger(MessageProducer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            // Logger.getLogger(MessageProducer.class.getName()).log(Level.SEVERE, null, ex);                    
        }
    }

    private void handleFilenameStdin(Message.Type messageTypeForStdin) {
        try {
            inputQueue.put(new Message(messageTypeForStdin, producerParameters.getStdinName()));
        } catch (InterruptedException ex) {
            // Logger.getLogger(MessageProducer.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        Message.Type messageTypeForFiles = Type.HASH_FILE;
        Message.Type messageTypeForStdin = Type.HASH_STDIN;
        // we want to calculate hashes for the files in the check file only
        if (producerParameters.getFilenamesFromCheckFile() != null) {

            if (!producerParameters.getListFilter().isHashingRequired()) {
                messageTypeForFiles = Type.DONT_HASH_FILE;
                messageTypeForStdin = Type.DONT_HASH_STDIN;                
            }
            
            for (String filename : producerParameters.getFilenamesFromCheckFile()) {
                // if <stdin> occurs in the check file
                if (filename.equals(producerParameters.getStdinName())) {
                    handleFilenameStdin(messageTypeForStdin);
                } else {
                    handleFilename(filename, true, messageTypeForFiles);
                }
            }
            messageTypeForFiles = Type.DONT_HASH_FILE;
            messageTypeForStdin = Type.DONT_HASH_STDIN;
        }

        // stdin wanted on the command line?
        if (producerParameters.isStdinForFilenamesFromArgs()) {
            handleFilenameStdin(messageTypeForStdin);
        }

        // all file parameters (files and dirs)
        for (String filename : allFiles) {
            handleFilename(filename, false, messageTypeForFiles);
        }

        // add exit message
        try {
            inputQueue.put(new Message(Type.EXIT));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
