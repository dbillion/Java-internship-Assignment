package com.playtech.assignment.moduular.task.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvReader {


    public static <T> List<T> readFromCSV(String filePath, Function<String[], T> mappingFunction) throws IOException {
        try (Stream<String> stream = Files.lines(Path.of(filePath))) {
            return stream
                    .skip(1) // Assuming the first line is a header
                    .map(line -> line.split(","))
                    .map(mappingFunction)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
