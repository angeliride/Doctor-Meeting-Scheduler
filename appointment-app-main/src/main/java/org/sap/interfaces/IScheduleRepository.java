package org.sap.interfaces;

import org.sap.models.Schedule;

import java.util.List;
import java.util.UUID;

public interface IScheduleRepository {
	List<Schedule> getSchedules();
	Schedule getScheduleById(UUID uuid);
	void addSchedule(Schedule schedule);
	void modifySchedule(Schedule schedule);
	void deleteSchedule(Schedule schedule);
	void load();
	void save();
}
