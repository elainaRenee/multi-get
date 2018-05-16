package com.github;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class DownloadCallable implements Callable<Chunk> {

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

    public Chunk call() throws Exception {
        byte[] chunkArray = null;
        try {
            // Connect to the URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set the range of byte to download
            String byteRange = startByte + "-" + endByte;
            conn.setRequestProperty("Range", "bytes=" + byteRange);

            // check for valid response code
            int code = conn.getResponseCode();
            //if (code / 100 != 2)


            // get the input stream
            InputStream in = conn.getInputStream();
            long size = endByte - startByte;


            chunkArray = IOUtils.toByteArray(in, size);

        } catch (Exception e) {
            // TODO: throw runtime exception
            e.printStackTrace();
        }

        return new Chunk(chunkArray, getPosition());
    }

}
