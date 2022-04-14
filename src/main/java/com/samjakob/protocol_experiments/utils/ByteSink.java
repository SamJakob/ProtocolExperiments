package com.samjakob.protocol_experiments.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Simulates a byte-by-byte writeable stream for demonstration purposes.
 * Equivalent to a DataInputStream.
 */
public class ByteSink {

    private final List<Byte> bytes;

    /**
     * Simulates a sink of bytes, where data can be written a certain number of
     * bytes at a time.
     * This is useful for simulating DataOutputStreams.
     */
    public ByteSink() {
        this.bytes = new ArrayList<>();
    }

    /**
     * Writes a byte into the sink, all of which can later be retrieved with
     * getBytes.
     * @param value The byte to add.
     */
    public void writeByte(byte value) {
        this.bytes.add(value);
    }

    /**
     * Empties all the stored bytes.
     */
    public void clear() {
        this.bytes.clear();
    }

    /**
     * Returns the number of available bytes that can be retrieved with
     * getBytes.
     * @return 0 if there are no bytes available, otherwise the number of
     * available bytes.
     *
     * @see ByteSink#getBytes()
     */
    public int available() {
        return bytes.size();
    }

    /**
     * Fetches all the bytes that were written since the stream was last
     * cleared and clears the stream.
     * @return The written bytes.
     */
    public byte[] getBytes() {
        byte[] result = new byte[bytes.size()];
        IntStream.range(0, bytes.size())
                .parallel()
                .forEach(i -> result[i] = bytes.get(i));
        clear();
        return result;
    }

}
