package org.sap.services;

import org.sap.interfaces.ILogRepository;
import org.sap.models.Log;
import org.sap.util.MenuUtil;

import java.util.List;
import java.util.Scanner;

public class LogService {
	private final ILogRepository logRepository;

	public LogService(ILogRepository logRepository) {
		this.logRepository = logRepository;
	}

	public void addLog(Log log) {
		logRepository.addLog(log);
	}

	public void displayLogs() {
		List<String> lines = logRepository.getLogLines();

		Scanner scanner = new Scanner(System.in);
		final int PAGE_SIZE = 15;
		int totalPages = (lines.size() + PAGE_SIZE - 1) / PAGE_SIZE;
		int currentPage = 0;

		if (totalPages > 1) {
			while (true) {
				int start = currentPage * PAGE_SIZE;
				int end = Math.min(start + PAGE_SIZE, lines.size());

				System.out.println("Strona " + (currentPage + 1) + "/" + totalPages);
				System.out.println("-----------");
				for (int i = start; i < end; i++)
					System.out.println(lines.get(i));
				System.out.println("-----------");
				System.out.println("[1] Poprzednia strona");
				System.out.println("[2] Następna strona");
				System.out.println("[3] Powrót");
				System.out.println();

				int choice = MenuUtil.getInt(scanner, "Wybierz opcję: ");
				switch (choice) {
					case 1 -> {
						if (currentPage > 0)
							currentPage--;
					}
					case 2 -> {
						if (currentPage < totalPages - 1)
							currentPage++;
					}
					case 3 -> {
						return;
					}
				}
			}
		} else {
			lines.forEach(System.out::println);
		}
	}
}
