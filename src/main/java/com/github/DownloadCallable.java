package com.github;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.Utils.exitOnFatalError;

/**
 * downloads part of a file
 */
public class DownloadCallable implements Callable<Chunk> {

    private final static Logger LOGGER = Logger.getLogger(DownloadCallable.class.getName());

    public final int position;
    public final URL url;
    public final int startByte;
    public final int endByte;

    public DownloadCallable(int position, URL url, int startByte, int endByte) {
        this.position = position;
        this.url = url;
        this.startByte = startByte;
        this.endByte = endByte;
    }

    public int getPosition() {
        return position;
    }

    public URL getUrl() {
        return url;
    }

    public int getStartByte() {
        return startByte;
    }

    public int getEndByte() {
        return endByte;
    }

    /**
     * downloads part of a file using a range request
     * @return downloaded chunk
     * @throws Exception
     */
    public Chunk call() throws Exception {
        LOGGER.log(Level.INFO, "Downloading chunk " + getPosition() + " starting at byte " + startByte + " and ending at byte " + endByte);

        byte[] chunkArray = null;
        try {
            // Connect to the URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set the range of byte to download
            String byteRange = startByte + "-" + endByte;
            conn.setRequestProperty("Range", "bytes=" + byteRange);

            // get the input stream
            InputStream in = conn.getInputStream();

            // get byte size
            long size = endByte - startByte;

            chunkArray = IOUtils.toByteArray(in, size);

        } catch (ConnectException e) {
            exitOnFatalError("Unable to connect to URL", e);

        } catch (Exception e) {
            exitOnFatalError("Error occurred in requesting chunk " + getPosition() + " from URL " + url, e);
        }

        LOGGER.log(Level.INFO, "Finished downloading chunk " + getPosition() + " starting at byte " + startByte + " and ending at byte " + endByte);

        return new Chunk(chunkArray, getPosition());
    }

}
