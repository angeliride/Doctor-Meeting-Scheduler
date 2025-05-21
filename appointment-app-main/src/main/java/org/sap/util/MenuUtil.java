package org.sap.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class MenuUtil {
    public static String getString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String str = scanner.nextLine();

            if (!str.isBlank())
                return str;
        }
    }

    public static int getInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException _) {

            }
        }
    }

    public static DayOfWeek getDayOfWeek(Scanner scanner, String prompt) {
        while (true) {
            System.out.println();
            System.out.println("[1] Poniedziałek");
            System.out.println("[2] Wtorek");
            System.out.println("[3] Środa");
            System.out.println("[4] Czwartek");
            System.out.println("[5] Piątek");
            System.out.println("[6] Sobota");
            System.out.println("[7] Niedziela");
            System.out.println();

            int choice = getInt(scanner, prompt);

            if (choice >= 1 && choice <= 7) {
                return DayOfWeek.of(choice);
            }
        }
    }

    public static LocalTime getLocalTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return LocalTime.parse(input);
            } catch (DateTimeParseException _) {

            }
        }
    }
}
