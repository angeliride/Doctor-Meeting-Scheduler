package org.sap.models;

import java.time.LocalDate;

public class ScheduleWithDate {
    private final Schedule schedule;
    private final LocalDate date;

    public ScheduleWithDate(Schedule schedule, LocalDate date) {
        this.schedule = schedule;
        this.date = date;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public LocalDate getDate() {
        return date;
    }
}
