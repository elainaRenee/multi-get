package com.github;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.Utils.exitOnFatalError;
import static com.github.Utils.getFileName;

/* downloads part of a file, making a new request for each chunk */

public class Download {

    private final static Logger LOGGER = Logger.getLogger(Download.class.getName());

    private final URL downloadUrl; // required
    private final String outputFile; // optional
    private final int chunkSize; // optional
    private final int chunks; // optional
    private final boolean parallel; // optional

    private Download(DownloadBuilder builder) {
        this.downloadUrl = builder.downloadUrl;
        this.chunks = builder.chunks;
        this.chunkSize = builder.chunkSize;
        this.parallel = builder.parallel;
        this.outputFile = builder.outputFile;
    }

    public static class DownloadBuilder {

        // default values for chunk and chunk size. Default size is 1 MiB or 1,049,000 bytes
        private static final int DEFAULT_CHUNK_SIZE = 1049000;
        private static final int DEFAULT_CHUNKS = 4;

        private final URL downloadUrl;
        private String outputFile;
        private int chunkSize;
        private int chunks;
        private boolean parallel;

        public DownloadBuilder(URL downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public DownloadBuilder outputFile(String outputFile) {
            this.outputFile = outputFile != null && !outputFile.isEmpty() ? outputFile : getFileName(this.downloadUrl);
            return this;
        }

        public DownloadBuilder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize == 0 ? DEFAULT_CHUNK_SIZE : chunkSize * DEFAULT_CHUNK_SIZE;;
            return this;
        }

        public DownloadBuilder chunks(int chunks) {
            this.chunks = chunks == 0 ? DEFAULT_CHUNKS : chunks;
            return this;
        }

        public DownloadBuilder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public Download build() {
            return new Download(this);
        }
    }

    public URL getDownloadUrl() {
        return downloadUrl;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunks() {
        return chunks;
    }

    public boolean isParallel() {
        return parallel;
    }

    /**
     * downloads chunks either sequentially or in parallel and saves to disk
     */
    public void start() {
        LOGGER.log(Level.INFO, "Started download at " + Instant.now());
        // results in memory from each download chunk
        byte[][] downloads = new byte[getChunks()][];
        long startByte = 0;
        boolean errors = false;

        if (isParallel()) {
            // create a thread pool for chunk downloads
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(getChunks());
            CompletionService<Chunk> completionService = new ExecutorCompletionService<Chunk>(executor);

            // keeps track of futures
            List<Future<Chunk>> futures = new ArrayList<Future<Chunk>>();

            // create thread for each chunk and start downloads
            for(int i = 0; i < chunks; i++) {
                long endByte = startByte + this.chunkSize;
                DownloadCallable callable = new DownloadCallable(i, getDownloadUrl(), startByte, endByte);
                futures.add(completionService.submit(callable));
                startByte = endByte + 1;
            }

            while(futures.size() > 0 && !errors) {
                try {
                    // block until a callable completes
                    Future<Chunk> resultFuture = completionService.take();
                    Chunk chunk = resultFuture.get();
                    downloads[chunk.getPosition()] = chunk.getChunk();
                    futures.remove(resultFuture);
                } catch (InterruptedException e) {
                    int completeFutures = getChunks() - futures.size();
                    exitOnFatalError("Error in downloading chunks. " + completeFutures + " of " + getChunks() + " downloads were successful", e);
                    return;
                } catch (ExecutionException e) {
                    int completeFutures = getChunks() - futures.size();
                    exitOnFatalError("Error in downloading chunks. " + completeFutures + " of " + getChunks() + " downloads were successful", e);
                    return;
                }
            }

            executor.shutdown();
        } else {
            // download chunks sequentially

            for(int i = 0; i < chunks; i++) {
                long endByte = startByte + getChunkSize() - 1;

                DownloadCallable callable = new DownloadCallable(i, getDownloadUrl(), startByte, endByte);
                Chunk chunk = null;
                try {
                    chunk = callable.call();
                } catch (Exception e) {
                    exitOnFatalError("Error in downloading chunk " + i, e);
                }
                downloads[i] = chunk.getChunk();
                startByte = endByte + 1;
            }
        }

        LOGGER.log(Level.INFO, "Merging chunks");

        // merge chunks
        byte[] finalResult = downloads[0];
        for(int i = 1; i < downloads.length; i++) {
            byte[] joinedArray = Arrays.copyOf(finalResult, finalResult.length + downloads[i].length);
            System.arraycopy(downloads[i], 0, joinedArray, finalResult.length, downloads[i].length);
            finalResult = joinedArray;
        }

        LOGGER.log(Level.INFO, "Saving chunks to file");

        createFile(finalResult);

        LOGGER.log(Level.INFO, "Finished download at " + Instant.now());
    }

    /**
     * creates and saves a file to the current directory
     * @param bytes
     */
    private void createFile(byte[] bytes) {
        File file = new File(getOutputFile());
        try {
            FileUtils.writeByteArrayToFile(file, bytes, 0, getChunks() * getChunkSize());
        } catch (FileNotFoundException e) {
            exitOnFatalError("Problem saving download with file name. Please try a different file name", e);
        } catch (IOException e) {
            exitOnFatalError("Error saving file. Please try again.", e);
        }
    }

}
