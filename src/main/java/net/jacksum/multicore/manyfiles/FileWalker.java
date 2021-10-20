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

import java.nio.file.*;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileWalker
 */
public class FileWalker {

    private final Path path;
    private final int depth;
    private final boolean followSymlinksToDirs;
    private final boolean followSymlinksToFiles;
    private final BlockingQueue<Message> queue;
    private final Message.Type messageTypeForFiles;
    private final Path outputFile;
    private final Path errorFile;
    private final boolean unlockAllUnixFileTypes;
    private final boolean unlockAllWindowsFileTypes;
    private final static boolean onWindows = System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows");

    public FileWalker(Message.Type messageTypeForFiles, ProducerParameters producerParameters, 
            Path path, 
            BlockingQueue<Message> queue) {
        this.messageTypeForFiles = messageTypeForFiles;
        this.depth = producerParameters.getDepth();
        this.followSymlinksToDirs = !producerParameters.isDontFollowSymlinksToDirectories();
        this.followSymlinksToFiles = !producerParameters.isDontFollowSymlinksToFiles();
        this.path = path;
        this.queue = queue;
        this.unlockAllUnixFileTypes = producerParameters.isUnlockAllUnixFileTypes();
        this.unlockAllWindowsFileTypes = producerParameters.isUnlockAllWindowsFileTypes();
        if (producerParameters.isOutputFile()) {
            this.outputFile = Paths.get(producerParameters.getOutputFile()).toAbsolutePath().normalize();
        } else {
            outputFile = null;
        }
        if (producerParameters.isErrorFile()) {
            this.errorFile = Paths.get(producerParameters.getErrorFile()).toAbsolutePath().normalize();
        } else {
            errorFile = null;
        }

    }

    public void walk() {
        Set<FileVisitOption> opts;
        if (followSymlinksToDirs) {
            opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            opts = EnumSet.noneOf(FileVisitOption.class); //Collections.emptySet();
        }
        
        TreeAction treeAction = new TreeAction(messageTypeForFiles, depth, queue, followSymlinksToDirs, followSymlinksToFiles, unlockAllUnixFileTypes, unlockAllWindowsFileTypes, outputFile, errorFile);
        try {
            Files.walkFileTree(path, opts, depth, treeAction);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    static class TreeAction implements FileVisitor<Path> {

        private final int depth;
        private final BlockingQueue<Message> queue;
        private final boolean followSymlinksToFiles;
        private final boolean followSymlinksToDirs;
        private final Message.Type messageTypeForFiles;
        private final boolean unlockAllUnixFileTypes;
        private final boolean unlockAllWindowsFileTypes;
        private final Path outputFile;
        private final Path errorFile;

        TreeAction(Message.Type messageTypeForFiles, int depth, BlockingQueue<Message> queue,
                   boolean followSymlinksToDirs, boolean followSymlinksToFiles,
                   boolean unlockAllUnixFileTypes, boolean unlockAllWindowsFileTypes,
                   Path outputFile, Path errorFile) {
            this.messageTypeForFiles = messageTypeForFiles;
            this.depth = depth;
            this.queue = queue;
            this.followSymlinksToFiles = followSymlinksToFiles;
            this.followSymlinksToDirs = followSymlinksToDirs;
            this.unlockAllUnixFileTypes = unlockAllUnixFileTypes;
            this.unlockAllWindowsFileTypes = unlockAllWindowsFileTypes;
            this.outputFile = outputFile;
            this.errorFile = errorFile;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
            if (!followSymlinksToFiles && Files.isSymbolicLink(path) && Files.isRegularFile(path)) {
                addMessageToQueue(new Message(Message.Type.INFO,
                        String.format("Ignoring \"%s\", because it is a symlink to a file.", path), path));
                return CONTINUE;
            }
            
            if (!followSymlinksToDirs && Files.isSymbolicLink(path) && Files.isDirectory(path)) {
                addMessageToQueue(new Message(Message.Type.INFO,
                        String.format("Ignoring \"%s\", because it is a symlink to a dir.", path), path));
                return CONTINUE;
            }

            /*if (Files.isFiles.isSymbolicLink(path)) {
                addMessageToQueue(new Message(Message.Type.INFO, path, String.format("\"%s\" is a symlink.", path)));
                return CONTINUE;
            }*/

            // depth < Integer.MAX_VALUE can cause a "Zugriff verweigert" on folders
            if (depth < Integer.MAX_VALUE && Files.isDirectory(path)) {
                addMessageToQueue(new Message(Message.Type.INFO_DIR_IGNORED,
                        String.format("\"%s\" is a directory, but the maximum number of allowed directory levels (%s) has been reached.", path, depth), path));
                return CONTINUE;
            }

            if (outputFile != null && Files.isRegularFile(path) && path.toAbsolutePath().normalize().equals(outputFile)) {
                addMessageToQueue(new Message(Message.Type.INFO,
                        String.format("\"%s\", won't be hashed, because it is the file where hashes will be stored.", path), path));
                return CONTINUE;
            }

            if (errorFile != null && Files.isRegularFile(path) && path.toAbsolutePath().normalize().equals(errorFile)) {
                addMessageToQueue(new Message(Message.Type.INFO,
                        String.format("\"%s\", won't be hashed, because it is the file where errors will be stored.", path), path));
                return CONTINUE;
            }

        if (Files.isRegularFile(path)
                || (!onWindows && unlockAllUnixFileTypes)
                || (onWindows && unlockAllWindowsFileTypes)
        ) {
            addMessageToQueue(new Message(messageTypeForFiles, null, path));
        } else {
            // a fifo for example (mkfifo myfifo)
            addMessageToQueue(new Message(Message.Type.ERROR, String.format("%s: is not a regular file.", path), path));
        }

//            addMessageToQueue(new Message(messageTypeForFiles, path));
            //logQueue.put(new Message(INFO, "File Walker: Object produced: " + queue.remainingCapacity() +" "+ message.getPath().toString()));
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path path, IOException exc) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                addMessageToQueue(new Message(Message.Type.ERROR, String.format("File System Cycle detected, ignoring: %s", path), path));
            } else {
                addMessageToQueue(new Message(Message.Type.ERROR, String.format("Unable to process: \"%s\": %s", path, exc), path));
            }
            return FileVisitResult.SKIP_SUBTREE;
        }

        private void addMessageToQueue(Message message) {
            try {
                queue.put(message);
            } catch (InterruptedException ex) {
                Logger.getLogger(FileWalker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
