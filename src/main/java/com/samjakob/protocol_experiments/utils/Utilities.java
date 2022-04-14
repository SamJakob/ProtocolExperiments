package com.samjakob.protocol_experiments.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Utilities {

    public static String formatBinary(byte b) {
        return String.format(
            "%8s",
            Integer.toBinaryString(b & 0xFF)
        ).replace(' ', '0');
    }

    public static void printBinary(byte b) {
        System.out.println(formatBinary(b));
    }

    public static void printBinaryArray(byte[] byteArray) {
        String[] binaryStringArray = new String[byteArray.length];
        IntStream.range(0, byteArray.length)
                .parallel()
                .forEach(i -> binaryStringArray[i] = formatBinary(byteArray[i]));

        System.out.println(Arrays.toString(binaryStringArray));
    }

}
