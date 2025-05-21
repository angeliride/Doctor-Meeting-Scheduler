package org.sap.models;

import java.time.LocalDateTime;

public class Log {
	private final String type;
	private final String description;
	private final LocalDateTime datetime;

	public Log(String type, String description) {
		this.type = type.toUpperCase();
		this.description = description;
		this.datetime = LocalDateTime.now();
	}

	public String toString() {
		return String.format("[%s] %s> %s", datetime.toString(), type, description);
	}
}

