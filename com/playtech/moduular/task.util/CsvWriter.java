package com.playtech.assignment.moduular.task.util;

import com.playtech.assignment.moduular.task.model.Event;
import com.playtech.assignment.moduular.task.model.User;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter {

    public static void writeBalances(final Path filePath, final List<User> users) {

        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            // Write the header line
            writer.append("user_id,balance\n");
            // Iterate over each user and write their ID and balance to the file
            for (final User user : users) {
                writer.append(String.valueOf(user.getUserId()))
                        .append(",")
                        .append(String.format("%.2f", user.getBalance())) // Format balance to ensure two decimal places
                        .append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    public static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }

}
