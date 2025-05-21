package org.sap.interfaces.impl;

import org.sap.interfaces.IAppointmentRepository;
import org.sap.interfaces.IScheduleRepository;
import org.sap.models.Appointment;
import org.sap.models.Schedule;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AppointmentRepository implements IAppointmentRepository {
    private final String FILE_NAME = "appointments.csv";
    private final List<Appointment> appointments;
    private final IScheduleRepository scheduleRepository;

    public AppointmentRepository(IScheduleRepository scheduleRepository) {
        this.appointments = new ArrayList<>();
        this.scheduleRepository = scheduleRepository;
        load();
    }

    @Override
    public List<Appointment> getAppointments() {
        return appointments.stream().map(Appointment::copy).collect(Collectors.toList());
    }

    @Override
    public Appointment getAppointmentById(UUID uuid) {
        return appointments.stream().filter(appointment -> appointment.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public Appointment addAppointment(Appointment appointment) {
        if (getAppointmentById(appointment.getUuid()) != null)
            throw new IllegalArgumentException("Appointment already exists");

        Appointment newAppointment = Appointment.copy(appointment);
        appointments.add(newAppointment);
        save();
        return newAppointment;
    }

    @Override
    public void editAppointment(Appointment appointment) {
        appointments.stream().filter(a -> a.getUuid().equals(appointment.getUuid())).findFirst().ifPresent(appointmentToEdit -> appointments.set(appointments.indexOf(appointmentToEdit), appointment));
        save();
    }

    @Override
    public void save() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Appointment appointment : appointments) {
                writer.write(appointment.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while saving accounts", e);
        }
    }

    @Override
    public void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'", e);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");

                if (fields.length != 6)
                    throw new IOException();

                UUID uuid = UUID.fromString(fields[0]);
                String scheduleId = fields[1];
                String patientId = fields[2];
                String doctorId = fields[3];
                Appointment.Status status = Appointment.Status.valueOf(fields[4]);
                LocalDate date = LocalDate.parse(fields[5]);

                if (status == Appointment.Status.Appointed) {
                    Schedule schedule = scheduleRepository.getScheduleById(UUID.fromString(scheduleId));
                    if (schedule != null) {
                        LocalDateTime endDateTime = LocalDateTime.of(date, schedule.getEndTime());
                        if (LocalDateTime.now().isAfter(endDateTime)) {
                            status = Appointment.Status.Finished;
                        }
                    }
                }

                Appointment appointment = new Appointment(uuid, status, patientId, scheduleId, doctorId, date);
                appointments.add(appointment);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read line in file '" + FILE_NAME + "'", e);
        }
    }
}
