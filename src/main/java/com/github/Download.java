package com.github;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.github.Utils.exit;
import static com.github.Utils.getFileName;

public class Download {

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
            this.outputFile = outputFile != null || !outputFile.isEmpty() ? getFileName(this.downloadUrl) : outputFile;
            return this;
        }

        public DownloadBuilder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize == 0 ? DEFAULT_CHUNK_SIZE : chunkSize;;
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

    public void start() {

        // results in memory from each download chunk
        byte[][] downloads = new byte[getChunks()][];
        int startByte = 0;
        boolean errors = false;

        if (isParallel()) {
            // create a thread pool for chunk downloads
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(getChunks());
            CompletionService<Chunk> completionService = new ExecutorCompletionService<Chunk>(executor);

            // keeps track of futures so we can cancel them if there is an error
            List<Future<Chunk>> futures = new ArrayList<Future<Chunk>>();

            // create thread for each chunk and start downloads
            for(int i = 0; i < chunks; i++) {
                int endByte = startByte + this.chunkSize - 1;
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
                    // TODO: add error handling
                    e.printStackTrace();
                    errors = true;
                } catch (ExecutionException e) {
                    // TODO: add error handling
                    e.printStackTrace();
                    errors = true;
                }
            }

            // cancel any remaining futures if an error occurs
            if (errors) {
                for(Future<Chunk> future : futures) {
                    future.cancel(true);
                }
            }

            executor.shutdown();
        } else {
            for(int i = 0; i < chunks; i++) {
                int endByte = startByte + this.chunkSize;
                DownloadCallable callable = new DownloadCallable(i, getDownloadUrl(), startByte, endByte);
                Chunk chunk = null;
                try {
                    chunk = callable.call();
                } catch (Exception e) {
                    // TODO: add error handling
                    e.printStackTrace();
                    errors = true;
                }
                downloads[i] = chunk.getChunk();

                if (errors)
                    break;
            }
        }

        if (errors) {
            // TODO: create new method for exit
            exit("Error occured in downloading file. Please try again");
        }

        // merge chunks
        byte[] finalResult = null;
        for(byte[] download : downloads) {
            finalResult = ArrayUtils.addAll(finalResult, download);
        }

        createFile(finalResult);
    }

    private void createFile(byte[] bytes) {
        File file = new File(getOutputFile());
        try {
            FileUtils.writeByteArrayToFile(file, bytes, 0, getChunks() * getChunkSize());
        } catch (IOException e) {
            // TODO: add error handling
            e.printStackTrace();
        }
    }

}
