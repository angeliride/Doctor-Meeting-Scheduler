package org.sap.interfaces.impl;

import org.sap.interfaces.IScheduleRepository;
import org.sap.models.Office;
import org.sap.models.Schedule;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScheduleRepository implements IScheduleRepository {
    private final String FILE_NAME = "schedules.csv";

    private final List<Schedule> schedules;

    public ScheduleRepository() {
        this.schedules = new ArrayList<>();
        load();
    }

    @Override
    public List<Schedule> getSchedules() {
        return schedules.stream()
                .map(Schedule::copy)
                .collect(Collectors.toList());
    }

    @Override
    public Schedule getScheduleById(UUID uuid) {
        return schedules.stream()
                .filter(schedule -> schedule.getUuid().equals(uuid))
                .findFirst().orElse(null);
    }

    @Override
    public void addSchedule(Schedule schedule) {
        if (getScheduleById(schedule.getUuid()) != null)
            throw new IllegalArgumentException("Schedule already exists");

        Schedule newSchedule = Schedule.copy(schedule);
        schedules.add(newSchedule);
        save();
    }

    @Override
    public void modifySchedule(Schedule schedule) {
        schedules.stream().filter(s -> s.getUuid().equals(schedule.getUuid())).findFirst().ifPresent(scheduleToEdit -> schedules.set(schedules.indexOf(scheduleToEdit), schedule));
        save();
    }

    @Override
    public void deleteSchedule(Schedule schedule) {
        Schedule scheduleToDelete = schedules.stream().filter(s -> s.getUuid().equals(schedule.getUuid())).findFirst().orElse(null);
        if (scheduleToDelete != null) {
            scheduleToDelete.setActive(false);
            modifySchedule(scheduleToDelete);
        }
    }

    @Override
    public void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'", e);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");

                if (fields.length != 8)
                    throw new IOException();

                UUID uuid = UUID.fromString(fields[0]);
                LocalTime startTime = LocalTime.parse(fields[1]);
                LocalTime endTime = LocalTime.parse(fields[2]);
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(fields[3]);
                int officeNumber = Integer.parseInt(fields[4]);
                String officeAddress = fields[5];
                String doctorId = fields[6];
                boolean active = Boolean.parseBoolean(fields[7]);

                Office office = new Office(officeNumber, officeAddress);
                Schedule schedule = new Schedule(uuid, startTime, endTime, dayOfWeek, office, doctorId, active);
                schedules.add(schedule);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read line '" + FILE_NAME + "' in file '" + FILE_NAME + "'", e);
        }

    }

    @Override
    public void save() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'", e);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Schedule schedule : schedules) {
                writer.write(schedule.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while saving schedules", e);
        }

    }
}
