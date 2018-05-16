package com.github;

public class Chunk {

    // position of chunk
    public final int position;

    // chunk of file downloaded
    public final byte[] chunk;

    public Chunk(byte[] chunk, int position) {
        this.chunk = chunk;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public byte[] getChunk() {
        return chunk;
    }
}
