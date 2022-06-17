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

import net.jacksum.multicore.OSControl;
import net.loefflmann.sugar.io.NtfsAdsFinder;

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
    private final boolean scanAllUnixFileTypes;
    private final boolean scanNtfsAds;
    private final static boolean onWindows = OSControl.isWindows();

    public FileWalker(Message.Type messageTypeForFiles, ProducerParameters producerParameters,
                      Path path,
                      BlockingQueue<Message> queue) {
        this.messageTypeForFiles = messageTypeForFiles;
        this.depth = producerParameters.getDepth();
        this.followSymlinksToDirs = !producerParameters.isDontFollowSymlinksToDirectories();
        this.followSymlinksToFiles = !producerParameters.isDontFollowSymlinksToFiles();
        this.path = path;
        this.queue = queue;
        this.scanAllUnixFileTypes = producerParameters.scanAllUnixFileTypes();
        this.scanNtfsAds = producerParameters.isScanNtfsAds();
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

        TreeAction treeAction = new TreeAction(messageTypeForFiles, depth, queue, followSymlinksToDirs, followSymlinksToFiles, scanAllUnixFileTypes, scanNtfsAds, outputFile, errorFile);
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
        private final boolean scanAllUnixFileTypes;
        private final boolean scanNtfsAds;
        private final Path outputFile;
        private final Path errorFile;

        TreeAction(Message.Type messageTypeForFiles, int depth, BlockingQueue<Message> queue,
                   boolean followSymlinksToDirs, boolean followSymlinksToFiles,
                   boolean scanAllUnixFileTypes, boolean scanNtfsAds,
                   Path outputFile, Path errorFile) {
            this.messageTypeForFiles = messageTypeForFiles;
            this.depth = depth;
            this.queue = queue;
            this.followSymlinksToFiles = followSymlinksToFiles;
            this.followSymlinksToDirs = followSymlinksToDirs;
            this.scanAllUnixFileTypes = scanAllUnixFileTypes;
            this.scanNtfsAds = scanNtfsAds;
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

            // a symlink, but not to a regular file and not to a dir
            // important: this check must be after the checks for both Files.isRegularFile() and Files.isDirectory()
            if (!followSymlinksToFiles && Files.isSymbolicLink(path)) {
                addMessageToQueue(new Message(Message.Type.INFO,
                        String.format("Ignoring \"%s\", because it is a symlink to a special Unix file type.", path), path));
                return CONTINUE;
            }

            // a depth limit can cause a "Zugriff verweigert" on folders, so let's handle that case by our own code
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
                    // a named pipe for example (mkfifo myfifo)
                    || (!onWindows && scanAllUnixFileTypes)
            ) {
                addMessageToQueue(new Message(messageTypeForFiles, null, path));
                if (onWindows && scanNtfsAds) {
                    findNtfsAds(path);
                }
            } else {
                // a named pipe for example (mkfifo myfifo)
                addMessageToQueue(new Message(Message.Type.ERROR, String.format("%s: is not a regular file.", path), path));
            }

            // logQueue.put(new Message(INFO, "File Walker: Object produced: " + queue.remainingCapacity() +" "+ message.getPath().toString()));
            return CONTINUE;
        }

        // find NTFS Alternate Data Streams (ADS) in this path
        private void findNtfsAds(Path path) {
            try {
                List<String> list = NtfsAdsFinder.find(path);
                if (list != null) {
                    for (String entry : list) {
                        addMessageToQueue(new Message(messageTypeForFiles, null, entry));
                    }
                }
            } catch (IOException | InterruptedException e) {
                addMessageToQueue(new Message(Message.Type.ERROR, String.format("Cannot find alternate data streams, ignoring: %s", path), path));
            }
        }

        @Override
        public FileVisitResult postVisitDirectory(Path path, IOException exc) {
            // ADS attached to directories
            if (onWindows && scanNtfsAds) {
                findNtfsAds(path);
            }
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
