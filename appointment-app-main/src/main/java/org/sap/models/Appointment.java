package org.sap.models;

import java.time.LocalDate;
import java.util.UUID;

public class Appointment {
	public enum Status {
		Cancelled, Appointed, Finished
	}

	private final UUID uuid;
	private final String scheduleId;
	private final String patientId;
	private final String doctorId;
	private Status status;
	private final LocalDate date;

	public Appointment(UUID uuid, Status status, String patientId, String scheduleId, String doctorId, LocalDate date) {
		this.uuid = uuid;
		this.status = status;
		this.patientId = patientId;
		this.scheduleId = scheduleId;
		this.doctorId = doctorId;
		this.date = date;
	}
	public UUID getUuid() {
		return uuid;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public String getPatientId() {
		return patientId;
	}

	public String getDoctorId() {
		return doctorId;
	}

	public Status getStatus() {
		return status;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public static Appointment copy(Appointment appointment) {
		return new Appointment(appointment.getUuid(), appointment.getStatus(), appointment.getPatientId(), appointment.getScheduleId(), appointment.getDoctorId(), appointment.getDate());
	}

	public String toCsv() {
		return String.format("%s;%s;%s;%s;%s;%s", uuid, scheduleId, patientId, doctorId, status.toString(), date.toString());
	}
}
