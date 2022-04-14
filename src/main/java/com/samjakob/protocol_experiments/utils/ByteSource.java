package com.samjakob.protocol_experiments.utils;

/**
 * Simulates a byte-by-byte readable stream for demonstration purposes.
 * Equivalent to a DataInputStream.
 */
public class ByteSource {

    private final byte[] data;

    private int pointer;

    /**
     * Simulates a source of bytes that can return bytes a certain number at
     * a time by using a pointer.
     * This is useful for simulating DataInputStreams.
     *
     * @param data The byte array representing the actual source data.
     */
    public ByteSource(byte[] data) {
        this.data = data;
        this.pointer = 0;
    }

    /**
     * Whether there is another byte at the pointer's location.
     * @return True if there is, otherwise false.
     */
    public boolean hasNextByte() {
        return this.pointer < data.length;
    }

    /**
     * Returns the number of bytes available.
     * @return 0 if there are no bytes available, otherwise the number of bytes
     * available.
     */
    public int available() {
        return data.length - this.pointer;
    }

    /**
     * Fetches the next byte from the byte array and increments the pointer.
     * @return The next byte
     */
    public byte getNextByte() {
        return this.data[pointer++];
    }

    /**
     * Sets the pointer into the data byte array to the specified value.
     * @param pointer The new pointer value.
     */
    public void setPointer(int pointer) {
        if (this.pointer < 0 || this.pointer > data.length)
            throw new IllegalArgumentException("Pointer must be between 0 and data.length (upper bound exclusive)");
        this.pointer = pointer;
    }

    /**
     * An alias for setPointer(0);
     */
    public void restartPointer() {
        setPointer(0);
    }

}
