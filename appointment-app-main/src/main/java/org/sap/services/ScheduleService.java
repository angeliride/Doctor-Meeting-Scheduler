package org.sap.services;

import org.sap.interfaces.IScheduleRepository;
import org.sap.models.Account;
import org.sap.models.Schedule;

import java.util.List;
import java.util.stream.Collectors;

public class ScheduleService {
	private final IScheduleRepository scheduleRepository;

	public ScheduleService(IScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}

	public List<Schedule> getDoctorSchedules(Account doctor) {
		return scheduleRepository.getSchedules().stream()
				.filter(Schedule::isActive)
				.filter(schedule -> schedule.getDoctorId().equals(doctor.getUuid().toString()))
				.collect(Collectors.toList());
	}

	public void addSchedule(Schedule schedule) {
		scheduleRepository.addSchedule(schedule);
	}
	
	public void delete_schedule(Schedule schedule) {
		scheduleRepository.deleteSchedule(schedule);
	}
}
