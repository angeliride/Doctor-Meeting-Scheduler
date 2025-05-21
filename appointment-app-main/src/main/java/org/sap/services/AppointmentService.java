package org.sap.services;

import org.sap.interfaces.IAccountRepository;
import org.sap.interfaces.IAppointmentRepository;
import org.sap.interfaces.IScheduleRepository;
import org.sap.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppointmentService {
	private final IAppointmentRepository appointmentRepository;
	private final IScheduleRepository scheduleRepository;
	private final IAccountRepository accountRepository;

	public AppointmentService(IAppointmentRepository appointmentRepository, IScheduleRepository scheduleRepository, IAccountRepository accountRepository) {
		this.appointmentRepository = appointmentRepository;
		this.scheduleRepository = scheduleRepository;
		this.accountRepository = accountRepository;
	}

	public List<Appointment> getAllAppointments() {
		return appointmentRepository.getAppointments();
	}

	public Appointment addAppointment(Account patient, Account doctor, Schedule schedule, LocalDate date) {
		Appointment appointment = new Appointment(
				UUID.randomUUID(),
				Appointment.Status.Appointed,
				patient.getUuid().toString(),
				schedule.getUuid().toString(),
				doctor.getUuid().toString(),
				date
		);

		return appointmentRepository.addAppointment(appointment);
	}

	public void cancelAppointment(Appointment appointment) {
		appointment.setStatus(Appointment.Status.Cancelled);
		appointmentRepository.editAppointment(appointment);
	}

	public boolean isFinished(Appointment appointment) {
		if (appointment.getStatus() == Appointment.Status.Finished || appointment.getStatus() == Appointment.Status.Cancelled)
			return true;

		Schedule schedule = scheduleRepository.getScheduleById(UUID.fromString(appointment.getScheduleId()));
		if (schedule != null && schedule.isActive()) {
			LocalDateTime endDateTime = LocalDateTime.of(appointment.getDate(), schedule.getEndTime());
			return LocalDateTime.now().isAfter(endDateTime);
		}

		return true;
	}

	public boolean isEditable(Appointment appointment) {
		if (appointment.getStatus() == Appointment.Status.Finished || appointment.getStatus() == Appointment.Status.Cancelled)
			return false;

		Schedule schedule = scheduleRepository.getScheduleById(UUID.fromString(appointment.getScheduleId()));
		if (schedule != null && schedule.isActive()) {
			LocalDateTime endDateTime = LocalDateTime.of(appointment.getDate(), schedule.getEndTime());
			return !LocalDateTime.now().isAfter(endDateTime.minusDays(1));
		}

		return true;
	}

	public String getAppointmentString(Appointment appointment, Account.Type perspective) {
		Schedule schedule = scheduleRepository.getScheduleById(UUID.fromString(appointment.getScheduleId()));

		if (perspective != Account.Type.Administrator) {
			Account account;
			switch (perspective) {
				case Doctor ->
						account = accountRepository.getAccountById(UUID.fromString(appointment.getPatientId()));
				case Patient ->
						account = accountRepository.getAccountById(UUID.fromString(appointment.getDoctorId()));
				default -> throw new IllegalStateException("Unexpected perspective value: " + perspective);
			}

			return String.format("%s, %s - %s -- %s%s %s (%s)",
					appointment.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
					schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
					schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
					account.getType() == Account.Type.Doctor ? "dr " : "",
					account.getName(), account.getSurname(),
					schedule.getOffice().getAddress() + ", pokój nr " + schedule.getOffice().getNumber()
			);
		} else {
			Account doctor = accountRepository.getAccountById(UUID.fromString(appointment.getDoctorId()));
			Account patient = accountRepository.getAccountById(UUID.fromString(appointment.getPatientId()));

			return String.format("%s, %s - %s -- dr %s %s - %s %s (%s)",
					appointment.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
					schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
					schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
					doctor.getName(), doctor.getSurname(),
					patient.getName(), patient.getSurname(),
					schedule.getOffice().getAddress() + ", pokój nr " + schedule.getOffice().getNumber()
			);
		}
	}

	public boolean isScheduleAppointed(ScheduleWithDate schedule) {
		if (!schedule.getSchedule().isActive())
			return true;

		return appointmentRepository.getAppointments().stream()
				.filter(appointment -> appointment.getScheduleId().equals(schedule.getSchedule().getUuid().toString()))
				.filter(appointment -> appointment.getDate().equals(schedule.getDate()))
				.anyMatch(appointment -> !isFinished(appointment));
	}
}
