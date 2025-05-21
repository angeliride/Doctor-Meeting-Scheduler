package org.sap.interfaces;

import org.sap.models.Log;

import java.util.List;

public interface ILogRepository {
	void addLog(Log log);
	List<String> getLogLines();
}
