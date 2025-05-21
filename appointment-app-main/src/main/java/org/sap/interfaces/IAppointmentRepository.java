package org.sap.interfaces;

import org.sap.models.Appointment;

import java.util.List;
import java.util.UUID;

public interface IAppointmentRepository {
	List<Appointment> getAppointments();

	Appointment getAppointmentById(UUID id);

	Appointment addAppointment(Appointment appointment);

	void editAppointment(Appointment appointment);

	void save();

	void load();
}
