package org.sap.interfaces.impl;

import org.sap.interfaces.ILogRepository;
import org.sap.models.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogRepository implements ILogRepository {
    private final String FILE_NAME = "logs.txt";

    public LogRepository() {}

    @Override
    public void addLog(Log log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(log.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing log", e);
        }
    }

    @Override
    public List<String> getLogLines() {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading logs", e);
        }

        return lines;
    }
}
