/*


  Jacksum 3.2.0 - a checksum utility in Java
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

import java.io.*;
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
import org.n16n.sugar.io.NtfsAdsFinder;

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

                if (producerParameters.isPathAbsolute()) {
                    path = path.toAbsolutePath().normalize();
                }

                if (Files.isDirectory(path)) {
                    if (filenameIsInCheckFile) {
                        outputQueue.put(new Message(Type.ERROR, String.format("%s: directory found in check file, but a filename was expected.", path), path));
                    } else {
                        FileWalker fileWalker = new FileWalker(
                                messageTypeForFiles,
                                producerParameters,
                                path,
                                inputQueue);
                        fileWalker.walk();
                    }

                } else {
                    inputQueue.put(new Message(messageTypeForFiles, null, path));

                    if (onWindows && producerParameters.isScanNtfsAds()) {
                        // find NTFS Alternate Data Streams (ADS) in this path
                        try {
                            List<String> list = NtfsAdsFinder.find(path);
                            if (list != null) {
                                for (String entry : list) {
                                    inputQueue.put(new Message(messageTypeForFiles, null, entry));
                                }
                            }
                        } catch (IOException | InterruptedException e) {
                            inputQueue.put(new Message(Message.Type.ERROR, String.format("Cannot find alternate data stream, ignoring: %s", path), path));
                        }
                    }

                }
            } else {
                outputQueue.put(new Message(Type.ERROR, String.format("%s: does not exist.", path), path));
            }

        } catch (InvalidPathException e) {
            // On Windows, the JDK's Path class throws an InvalidPathException if there are illegal characters in the path.
            // However, in some cases those characters are legal, actually. Microsoft Windows supports a schema in order
            // to access partitions, e.g. "\\.\c:\", and to access NTFS ADS (alternate data stream), e.g.
            // "message.txt:ads:$DATA". So we have to check the validity by our own code if the Path class can't handle it.

            // Also, if the shell wasn't able to resolve wildcards and simply passes the wildcard string to the app.
            // Also, if the Windows File System Parser cannot transform the string that contains wildcards
            // to a Path object, it throws an InvalidPathException.
            if (onWindows) {
                if (specialWindowsFileExists(filename)) {
                    try {
                        inputQueue.put(new Message(messageTypeForFiles, null, filename));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        outputQueue.put(new Message(Type.ERROR, String.format("%s: not found.", filename), (Path) null));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                // POSIX path-names may not contain null characters.
                try {
                    outputQueue.put(new Message(Type.ERROR, String.format("%s: not found: %s", filename, e.getMessage()), (Path) null));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    // Logger.getLogger(MessageProducer.class.getName()).log(Level.SEVERE, null, ex);
                }
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

    public static boolean specialWindowsFileExists(String filename) {

        File file = new File(filename);
        if (file.isFile() && file.canRead()) { // i.e. a NTFS ADS file such as message.txt:secret:$DATA
            return true;
        }
        // further checks are required
        // filename could be a partition, such as "\\.\c:\", we need to check whether it is accessible

        InputStream is = null;

        try {
            is = new FileInputStream(filename);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
            }
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
