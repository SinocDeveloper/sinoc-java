package org.sinoc.shell.util;

import java.util.Optional;
import java.util.stream.Stream;

public class StreamUtil {

    /**
     * Stream or value or empty stream if value is null.
     */
    public static <T> Stream<T> streamOf(T value) {
        return Optional.ofNullable(value)
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
