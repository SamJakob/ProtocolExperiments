package com.samjakob.protocol_experiments.data;

import com.samjakob.protocol_experiments.utils.ByteSink;
import com.samjakob.protocol_experiments.utils.ByteSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.samjakob.protocol_experiments.data.VarLengthNumbers.*;
import static org.junit.jupiter.api.Assertions.*;

public class VarLengthNumbersTests {

    private ByteSink sink;

    @BeforeEach
    void init() {
        sink = new ByteSink();
    }

    @Test
    void canWriteValidVarInts() {
        // Write a negative number - should use maximum number of bytes for a
        // VarInt.
        writeVarInt(sink::writeByte, -1);
        assertEquals(5, sink.available(), "Negative number should use maximum number of bytes for a VarInt");

        // Write a different negative number - should still use maximum number
        // of bytes for a VarInt.
        sink.clear();
        writeVarInt(sink::writeByte, -35);
        assertEquals(5, sink.available(), "Negative number should use maximum number of bytes for a VarInt");

        // Write a positive number less than or equal to 7 bits - should use
        // 1 byte.
        sink.clear();
        writeVarInt(sink::writeByte, 35);
        assertEquals(1, sink.available(), "Number of size 7 bits or less should only use 1 byte");

        // Write zero - should only use 1 byte.
        sink.clear();
        writeVarInt(sink::writeByte, 0);
        assertEquals(1, sink.available(), "Zero should only use 1 byte");

        // Write 127 which is 7 bits - should use 1 byte.
        sink.clear();
        writeVarInt(sink::writeByte,  127);
        assertEquals(1, sink.available(), "Number of size 7 bits or less should only use 1 byte");

        // Write 255 which is 8 bits - should use 2 bytes.
        sink.clear();
        writeVarInt(sink::writeByte,  255);
        assertEquals(2, sink.available(), "Number of size 8 bits should use 2 bytes");

        // Write 315 which is less than or equal to 14 bits - should use
        // 2 bytes.
        sink.clear();
        writeVarInt(sink::writeByte,  315);
        assertEquals(2, sink.available(), "Number of size 14 bits or less should use 2 bytes");

        // Write 2^20, which is 20 bits (7 * 3 = 21 and 20 > 14 but 20 < 21) -
        // should use 3 bytes.
        sink.clear();
        writeVarInt(sink::writeByte,  (int) Math.pow(2, 20));
        assertEquals(3, sink.available(), "Number of size 20 bits should use 3 bytes");

        // Write Integer.MAX_VALUE - should use 5 bytes.
        sink.clear();
        writeVarInt(sink::writeByte,  Integer.MAX_VALUE);
        assertEquals(5, sink.available(), "Number of size max int value should use 5 bytes");

        // Write Integer.MIN_VALUE - should use 5 bytes.
        sink.clear();
        writeVarInt(sink::writeByte,  Integer.MIN_VALUE);
        assertEquals(5, sink.available(), "Number of size min int value should use 5 bytes");
    }

    @Test
    void canWriteValidVarLongs() {
        // Write a negative number - should use maximum number of bytes for a
        // VarInt.
        writeVarLong(sink::writeByte, -1);
        assertEquals(10, sink.available(), "Negative number should use maximum number of bytes for a VarLong");

        // Write a different negative number - should still use maximum number
        // of bytes for a VarInt.
        sink.clear();
        writeVarLong(sink::writeByte, -35);
        assertEquals(10, sink.available(), "Negative number should use maximum number of bytes for a VarLong");

        // Write a positive number less than or equal to 7 bits - should use
        // 1 byte.
        sink.clear();
        writeVarLong(sink::writeByte, 35);
        assertEquals(1, sink.available(), "Number of size 7 bits or less should only use 1 byte");

        // Write zero - should only use 1 byte.
        sink.clear();
        writeVarLong(sink::writeByte, 0);
        assertEquals(1, sink.available(), "Zero should only use 1 byte");

        // Write 127 which is 7 bits - should use 1 byte.
        sink.clear();
        writeVarLong(sink::writeByte,  127);
        assertEquals(1, sink.available(), "Number of size 7 bits or less should only use 1 byte");

        // Write 255 which is 8 bits - should use 2 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  255);
        assertEquals(2, sink.available(), "Number of size 8 bits should use 2 bytes");

        // Write 315 which is less than or equal to 14 bits - should use
        // 2 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  315);
        assertEquals(2, sink.available(), "Number of size 14 bits or less should use 2 bytes");

        // Write 2^20, which is 20 bits (7 * 3 = 21 and 20 > 14 but 20 < 21) -
        // should use 3 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  (long) Math.pow(2, 20));
        assertEquals(3, sink.available(), "Number of size 20 bits should use 3 bytes");

        // Write 2^36, which is 36 bits (7 * 6 = 42 and 36 > 35 but 36 < 42) -
        // should use 6 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  (long) Math.pow(2, 36));
        assertEquals(6, sink.available(), "Number of size 36 bits should use 6 bytes");

        // Write Integer.MAX_VALUE - should use 5 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  Integer.MAX_VALUE);
        assertEquals(5, sink.available(), "Number of size max int value should use 5 bytes");

        // Write Long.MAX_VALUE - should use 9 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  Long.MAX_VALUE);
        assertEquals(9, sink.available(), "Number of size max long value should use 9 bytes");

        // Write Integer.MIN_VALUE - should use 10 bytes.
        sink.clear();
        writeVarLong(sink::writeByte,  Integer.MIN_VALUE);
        assertEquals(10, sink.available(), "Number of size min int value should use 10 bytes");
    }

    @Test
    void canWriteAndReadVarInts() {
        // Define a list of values that should be written into the sink and
        // later successfully retrieved.
        //
        // This should contain edge cases - e.g., numbers on the boundary of
        // where the integer gets split (every 7 bits), negatives, 0, etc.
        int[] values = {
            0, 1, 2, 127, 128, 255, 256, 6969, 2097151, Integer.MAX_VALUE,
            -1, -2, -127, -128, -255, -256, Integer.MIN_VALUE
        };

        // Write each value from values in order into the sink.
        for (int value : values) {
            writeVarInt(sink::writeByte, value);
        }

        // Create a source from the sink and attempt to read-back the values.
        var source = new ByteSource(sink.getBytes());

        for (int i = 0; i < values.length; i++) {
            assertEquals(
                values[i], readVarInt(source::getNextByte),
                String.format("Failed to read value at index %d: %d. Value read from source did not match value written to sink.", i, values[i])
            );
        }

        // We can also assert that there are no bytes left over.
        assertEquals(0, source.available(), "There should be no bytes left over.");
    }

    @Test
    void refusesToReadInvalidVarInt() {
        // Write a full byte up to and including the maximum byte value for a
        // VarInt. This is going to contain an invalid final value because
        // the last value in the VarInt should not have its continuation bit
        // set, nor the other 3 most significant bits.
        for (int i = 0; i < 5; i++) sink.writeByte((byte) 0b1111_1111);

        var source = new ByteSource(sink.getBytes());

        // If a call to readVarInt with the source does not throw a runtime
        // exception, the error checking code is incorrect here for the above
        // reason.
        assertThrows(
            RuntimeException.class,
            () -> readVarInt(source::getNextByte)
        );
    }

    @Test
    void canWriteAndReadVarLongs() {
        // Define a list of values that should be written into the sink and
        // later successfully retrieved.
        //
        // This should contain edge cases - e.g., numbers on the boundary of
        // where the long integer gets split (every 7 bits), negatives, 0, etc.
        long[] values = {
                0, 1, 2, 127, 128, 255, 256, 6969, 2097151, Integer.MAX_VALUE,
                Long.MAX_VALUE,
                -1, -2, -127, -128, -255, -256, Integer.MIN_VALUE,
                Long.MIN_VALUE
        };

        // Write each value from values in order into the sink.
        for (long value : values) {
            writeVarLong(sink::writeByte, value);
        }

        // Create a source from the sink and attempt to read-back the values.
        var source = new ByteSource(sink.getBytes());

        for (int i = 0; i < values.length; i++) {
            assertEquals(
                    values[i], readVarLong(source::getNextByte),
                    String.format("Failed to read value at index %d: %d. Value read from source did not match value written to sink.", i, values[i])
            );
        }

        // We can also assert that there are no bytes left over.
        assertEquals(0, source.available(), "There should be no bytes left over.");
    }

    @Test
    void refusesToReadInvalidVarLong() {
        // Write a full byte up to and including the maximum byte value for a
        // VarLong. This is going to contain an invalid final value because
        // the last value in the VarLong should not have its continuation bit
        // set, nor the other 7 most significant bits.
        for (int i = 0; i < 10; i++) sink.writeByte((byte) 0b1111_1111);

        var source = new ByteSource(sink.getBytes());

        // If a call to readVarInt with the source does not throw a runtime
        // exception, the error checking code is incorrect here for the above
        // reason.
        assertThrows(
                RuntimeException.class,
                () -> readVarLong(source::getNextByte)
        );
    }

}
