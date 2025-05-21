package org.sap.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public class Schedule {
	private final UUID uuid;
	private final LocalTime startTime;
	private final LocalTime endTime;
	private final DayOfWeek dayOfWeek;
	private final Office office;
	private final String doctorId;
	private boolean active;

	public Schedule(UUID uuid, LocalTime start_time, LocalTime end_time, DayOfWeek day_of_week, Office office, String doctorId, boolean active) {
		this.uuid = uuid;
		this.startTime = start_time;
		this.endTime = end_time;
		this.dayOfWeek = day_of_week;
		this.office = office;
		this.doctorId = doctorId;
		this.active = active;
	}

	public UUID getUuid() {
		return uuid;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public Office getOffice() {
		return office;
	}

	public String getDoctorId() {
		return doctorId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public static Schedule copy(Schedule schedule) {
		return new Schedule(
				schedule.getUuid(),
				schedule.getStartTime(),
				schedule.getEndTime(),
				schedule.getDayOfWeek(),
				Office.copy(schedule.getOffice()),
				schedule.getDoctorId(),
				schedule.isActive()
		);
	}

	public String toCsv() {
		return String.format("%s;%s;%s;%s;%s;%s;%s",
				uuid,
				startTime.toString(),
				endTime.toString(),
				dayOfWeek.toString(),
				office.toCsv(),
				doctorId,
				active
		);
	}
}
