package com.samjakob.protocol_experiments.data;

import com.samjakob.protocol_experiments.interfaces.ReaderInterface;
import com.samjakob.protocol_experiments.interfaces.WriterInterface;

/**
 * Contains a reference implementation for ChungusChat's VarInt and VarLong.
 *
 * These were originally going to either have a fixed 3-byte length prefix to
 * determine the length of the remaining data in continuation bytes OR an
 * additional separate sign bit.
 *
 * However, as the use case for these variable length integers doesn't really
 * feature negative numbers, these have ended up mirroring the Minecraft
 * implementation for VarInts where negative numbers require the maximum
 * length.
 *
 * For more information see:
 * https://wiki.vg/Protocol#VarInt_and_VarLong
 */
public class VarLengthNumbers {

    /**
     * Represents 0b1000_0000. In Java bytes are signed, so we can't just use
     * that value in a byte object.
     */
    private static final byte CONTINUE_BIT   = -128;
    /**
     * Represents 0b0111_1111. The ~ operator means bitwise compliment, which
     * essentially flips all the bits in the byte.
     */
    private static final byte SEGMENT_BITS   = ~CONTINUE_BIT;

    public static void writeVarInt(WriterInterface writer, int value) {
        do {
            // Start by writing the value. This automatically handles the edge
            // case where value = 0.
            writer.writeByte((byte) (
                // Write the segment bits (7 least significant bits) of the
                // value and OR it with the appropriate 'Continue Bit' value.
                (value & SEGMENT_BITS) |
                // The 'Continue Bit' is the most significant bit and should be
                // 1 if we need to write more bytes to send the entire value.
                //
                // We check this by seeing if there are any bits in 'value',
                // (excluding the segment bits we just wrote) that are set.
                // If there are, then we know that we need to write another
                ((value & ~SEGMENT_BITS) != 0 ? CONTINUE_BIT : 0)
            ));

            // Shift value right, with the sign bit.
            value >>>= 7;
        } while (value != 0);

        // Note that we needn't check if the value is too big for a VarInt,
        // because Java's compiler will handle that for us, having typed the
        // value as an integer.
    }

    public static void writeVarLong(WriterInterface writer, long value) {
        do {
            // Start by writing the value. This automatically handles the edge
            // case where value = 0.
            writer.writeByte((byte) (
                // Write the segment bits (7 least significant bits) of the
                // value and OR it with the appropriate 'Continue Bit' value.
                (value & SEGMENT_BITS) |
                // The 'Continue Bit' is the most significant bit and should be
                // 1 if we need to write more bytes to send the entire value.
                //
                // We check this by seeing if there are any bits in 'value',
                // (excluding the segment bits we just wrote) that are set.
                // If there are, then we know that we need to write another
                ((value & ~SEGMENT_BITS) != 0 ? CONTINUE_BIT : 0)
            ));

            // Shift value right, with the sign bit.
            value >>>= 7;
        } while (value != 0);

        // Note that we needn't check if the value is too big for a VarLong,
        // because Java's compiler will handle that for us, having typed the
        // value as a long integer.
    }

    public static int readVarInt(ReaderInterface reader) {
        // The index of the current byte being processed.
        // Incremented every time a new byte is fetched due to the continuation
        // bit being set.
        byte currentByteIndex = 0;

        // The value of the current byte being processed.
        // Set every time a new byte is fetched.
        byte currentByte;

        // The final resulting value.
        int value = 0;

        do {
            // Read the next byte from the input stream.
            currentByte = reader.readByte();

            // Read the current byte's segment bits and write them into the
            // resulting value. Offset the bits by the number of bytes we've
            // already processed.
            value |= (currentByte & SEGMENT_BITS) << (currentByteIndex * 7);
            currentByteIndex++;

            // If we're on the last byte (i.e., currentByteIndex is 5), and we
            // get a set continuation bit or more than 4 overflow bytes, we
            // know that something's gone wrong.
            //
            // We factor in 1 byte for our continuation bit and determine
            // maxPosition trivially from the number of bits in a regular
            // (4-byte) integer = 8 * 4 = 32 bits.
            // (7 * 5 = 35) - (maxPosition = 32) + (1 = continuation bit)
            // = (35 - 32) + 1 = 4.
            //
            //                             ┌──────────────────────────────────┐
            //                             │ In other words, this is the only │
            //                             │ bit that can be set in the last  │
            //                             │ byte of a VarLong.               │
            //                             └───────────────────┬──────────────┘
            if (currentByteIndex == 5 && (currentByte & 0b1111_0000) != 0)
                throw new RuntimeException("Invalid VarInt");

        } while ((currentByte & CONTINUE_BIT) != 0);

        return value;
    }

    public static long readVarLong(ReaderInterface reader) {
        // The index of the current byte being processed.
        // Incremented every time a new byte is fetched due to the continuation
        // bit being set.
        byte currentByteIndex = 0;

        // The value of the current byte being processed.
        // Set every time a new byte is fetched.
        byte currentByte;

        // The final resulting value.
        long value = 0;

        do {

            // Read the next byte from the input stream.
            currentByte = reader.readByte();

            // Read the current byte's segment bits and write them into the
            // resulting value. Offset the bits by the number of bytes we've
            // already processed.
            value |= (long) (currentByte & SEGMENT_BITS) << (currentByteIndex * 7);
            currentByteIndex++;

            // If we're on the last byte (i.e., currentByteIndex is 10), and we
            // get a set continuation bit or more than 7 overflow bytes, we
            // know that something's gone wrong.
            //
            // We factor in 1 byte for our continuation bit and determine
            // maxPosition trivially from the number of bits in a long (8-byte)
            // integer = 8 * 8 = 64 bits.
            // (7 * 10 = 70) - (maxPosition = 64) + (1 = continuation bit)
            // = (70 - 64) + 1 = 7.
            //
            // In this case, the only time the bit in the last byte is set, is
            // if the number was negative, because the sign bit overflows.
            //
            //                             ┌──────────────────────────────────┐
            //                             │ In other words, this is the only │
            //                             │ bit that can be set in the last  │
            //                             │ byte of a VarLong.               │
            //                             └───────────────────────┬──────────┘
            if (currentByteIndex == 10 && (currentByte & 0b1111_1110) != 0)
                throw new RuntimeException("Invalid VarLong");

        } while ((currentByte & CONTINUE_BIT) != 0);

        return value;
    }

}
