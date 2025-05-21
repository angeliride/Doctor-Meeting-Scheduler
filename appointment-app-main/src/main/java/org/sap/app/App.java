package org.sap.app;

import org.sap.interfaces.IAuthorization;
import org.sap.models.*;
import org.sap.services.*;
import org.sap.util.MenuUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class App {

    private final IAuthorization authorization;
    private final AccountService accountService;
    private final AppointmentService appointmentService;
    private final ScheduleService scheduleService;
    private final LogService logService;
    private final PasswordService passwordService;

    private UUID loggedAccountUuid;

    public App(IAuthorization authorization,
               AccountService accountService,
               AppointmentService appointmentService,
               ScheduleService scheduleService,
               LogService logService,
               PasswordService passwordService) {

        this.authorization = authorization;
        this.accountService = accountService;
        this.appointmentService = appointmentService;
        this.scheduleService = scheduleService;
        this.logService = logService;
        this.passwordService = passwordService;
    }

    void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("===============================");
        System.out.println("  SYSTEM UMAWIANIA WIZYT");
        System.out.println("===============================");
        while (running) {
            System.out.println();
            System.out.println("[1] Zaloguj się");
            System.out.println("[2] Zarejestruj się");
            System.out.println("[3] Wyjdź z aplikacji");
            System.out.println();

            int choice = MenuUtil.getInt(scanner, "Wybierz opcję: ");
            switch (choice) {
                case 1 -> {
                    loginMenu(scanner);
                    if (loggedAccountUuid != null)
                        mainMenu(scanner);
                }
                case 2 -> registerMenu(scanner);
                case 3 -> running = false;
                default -> System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
            }
        }

        scanner.close();
    }

    private void loginMenu(Scanner scanner) {
        String login = MenuUtil.getString(scanner, "Login: ");
        String password = MenuUtil.getString(scanner, "Hasło: ");

        Account account = authorization.login(login, password);
        if (account != null) {
            loggedAccountUuid = account.getUuid();
            logService.addLog(new Log("login", String.format("%s się zalogował.", login)));
        }
    }

    private void registerMenu(Scanner scanner) {
        String login = MenuUtil.getString(scanner, "Login: ");
        String password = MenuUtil.getString(scanner, "Hasło: ");
        String name = MenuUtil.getString(scanner, "Imię: ");
        String surname = MenuUtil.getString(scanner, "Nazwisko: ");

        boolean result = authorization.register(login, password, name, surname);
        if (result) {
            System.out.println("Rejestracja zakończona sukcesem. Możesz się teraz zalogować.");
            logService.addLog(new Log("register", String.format("%s założył konto.", login)));
        }
    }

    private void mainMenu(Scanner scanner) {
        System.out.println();
        System.out.println("Witaj, " + accountService.getAccountById(loggedAccountUuid).getName() + "!");

        while (true) {
            switch (accountService.getAccountById(loggedAccountUuid).getType()) {
                case Administrator -> adminMenu(scanner);
                case Doctor -> doctorMenu(scanner);
                case Patient -> patientMenu(scanner);
                default -> throw new IllegalStateException("Unexpected value: " + accountService.getAccountById(loggedAccountUuid).getType());
            }
        }
    }

    private void adminMenu(Scanner scanner) {
        System.out.println();
        System.out.println("[1] Zarządzanie kontami");
        System.out.println("[2] Wyświetl historię wizyt");
        System.out.println("[3] Wyświetl logi");
        System.out.println("[4] Zmień swoje hasło");
        System.out.println("[5] Wyjdź z aplikacji");
        System.out.println();

        int choice = MenuUtil.getInt(scanner, "Wybierz opcję: ");
        switch (choice) {
            case 1 -> {
                List<Account> accounts = accountService.getAllAccounts().stream().filter(account -> account.getType() != Account.Type.Administrator).toList();

                System.out.println("== Dostępne konta ==");
                boolean loop = true;
                while (loop) {
                    Map<Integer, Account> appointmentMap = new LinkedHashMap<>();
                    Integer i = 1;
                    for (Account account : accounts) {
                        appointmentMap.put(i, account);

                        String type = "-";
                        switch (account.getType()) {
                            case Administrator -> type = "Administrator";
                            case Doctor -> type = "Lekarz";
                            case Patient -> type = "Pacjent";
                        }

                        System.out.printf("[%d] %s: %s %s (%s)\n", i, type, account.getName(), account.getSurname(), account.getLogin());

                        i++;
                    }
                    System.out.println("[0] Powrót");
                    System.out.println();

                    int innerChoice = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                    if (innerChoice == 0)
                        break;

                    Account selectedAccount = appointmentMap.get(innerChoice);
                    if (selectedAccount == null) {
                        System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.\n");
                        continue;
                    }

                    System.out.println();

                    String type = "-";
                    switch (selectedAccount.getType()) {
                        case Administrator -> type = "Administrator";
                        case Doctor -> type = "Lekarz";
                        case Patient -> type = "Pacjent";
                    }
                    System.out.printf("%s: %s %s (%s)\n", type, selectedAccount.getName(), selectedAccount.getSurname(), selectedAccount.getLogin());

                    while (loop) {
                        System.out.println();
                        if (selectedAccount.getType() != Account.Type.Doctor)
                            System.out.println("[1] Ustaw jako lekarza");
                        else
                            System.out.println("[1] Ustaw jako pacjenta");
                        System.out.println("[2] Zresetuj hasło");
                        System.out.println("[3] Edytuj dane osobowe");
                        System.out.println("[4] Powrót");
                        System.out.println();

                        int innerChoice2 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                        switch (innerChoice2) {
                            case 1 -> {
                                Account.Type newType = selectedAccount.getType() == Account.Type.Patient ? Account.Type.Doctor : Account.Type.Patient;

                                selectedAccount.setType(newType);
                                accountService.editAccount(selectedAccount);
                                System.out.printf("Ustawiono użytkownika jako %s.\n", newType == Account.Type.Patient ? "pacjenta" : "lekarza");

                                loop = false;
                                logService.addLog(new Log("set-as-doctor", String.format("%s ustawił %s jako %s.", accountService.getAccountById(loggedAccountUuid).getLogin(), selectedAccount.getLogin(), newType)));
                            }
                            case 2 -> {
                                passwordService.resetPassword(selectedAccount);
                                logService.addLog(new Log("reset-password", String.format("%s zresetował hasło użytkownika %s.", accountService.getAccountById(loggedAccountUuid).getLogin(), selectedAccount.getLogin())));
                                loop = false;
                            }
                            case 3 -> {
                                String name = MenuUtil.getString(scanner, "Nowe imię (aktualnie: " + selectedAccount.getName() + "): ");
                                String surname = MenuUtil.getString(scanner, "Nowe nazwisko (aktualnie: " + selectedAccount.getSurname() + "): ");

                                selectedAccount.setName(name);
                                selectedAccount.setSurname(surname);
                                accountService.editAccount(selectedAccount);

                                System.out.println("Edytowano dane osobowe użytkownika.");
                                logService.addLog(new Log("edit-details", String.format("%s edytował dane osobowe użytkownika %s.", accountService.getAccountById(loggedAccountUuid).getLogin(), selectedAccount.getLogin())));
                                loop = false;
                            }
                            case 4 -> loop = false;
                        }
                    }

                    break;
                }
            }
            case 2 -> {
                List<Appointment> appointmentList = appointmentService.getAllAppointments().stream()
                        .filter(appointmentService::isFinished)
                        .toList();

                if (appointmentList.isEmpty()) {
                    System.out.println("Historia wizyt jest pusta.");
                    return;
                }

                for (Appointment appointment : appointmentList) {
                    String status;
                    switch (appointment.getStatus()) {
                        case Finished -> status = "Zakończona";
                        default -> status = "Anulowana";
                    }

                    System.out.printf("[%s] %s\n", status, appointmentService.getAppointmentString(appointment, Account.Type.Administrator));
                }
            }
            case 3 -> logService.displayLogs();
            case 4 -> {
                if (!changePassword(scanner))
                    return;
                logService.addLog(new Log("password-change", String.format("%s zmienił swoje hasło.", accountService.getAccountById(loggedAccountUuid).getLogin())));
                System.out.println("Hasło zostało zmienione.");
            }
            case 5 -> System.exit(0);
            default -> System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
        }
    }

    private void doctorMenu(Scanner scanner) {
        System.out.println();
        System.out.println("[1] Wyświetl grafik");
        System.out.println("[2] Wyświetl historię wizyt");
        System.out.println("[3] Edytuj swoje terminy");
        System.out.println("[4] Zmień swoje hasło");
        System.out.println("[5] Wyjdź z aplikacji");
        System.out.println();
        System.out.print("Wybierz opcję: ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1 -> {
                List<Appointment> appointments = appointmentService.getAllAppointments().stream()
                        .filter(appointment -> appointment.getDoctorId().equals(loggedAccountUuid.toString()))
                        .filter(appointment -> !appointmentService.isFinished(appointment))
                        .toList();

                if (appointments.isEmpty()) {
                    System.out.println("Grafik jest pusty.");
                    return;
                }

                System.out.println("== Grafik ==");
                boolean loop = true;
                while (loop) {
                    Map<Integer, Appointment> appointmentMap = new LinkedHashMap<>();
                    Integer i = 1;
                    for (Appointment appointment : appointments) {
                        appointmentMap.put(i, appointment);

                        System.out.printf("[%d] %s\n", i, appointmentService.getAppointmentString(appointment, Account.Type.Doctor));

                        i++;
                    }
                    System.out.println("[0] Powrót");
                    System.out.println();

                    int innerChoice = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                    if (innerChoice == 0) {
                        break;
                    }

                    Appointment selected = appointmentMap.get(innerChoice);
                    if (selected == null) {
                        System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.\n");
                        continue;
                    }

                    System.out.println();
                    System.out.println(appointmentService.getAppointmentString(selected, Account.Type.Doctor));

                    while (loop) {
                        System.out.println();
                        System.out.println("[1] Odwołaj wizytę");
                        System.out.println("[2] Powrót");
                        System.out.println();

                        int innerChoice2 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                        switch (innerChoice2) {
                            case 1 -> {
                                while (loop) {
                                    System.out.println();
                                    System.out.println("Czy na pewno chcesz odwołać tę wizytę?");
                                    System.out.println();
                                    System.out.println("[1] Tak");
                                    System.out.println("[2] Nie");
                                    System.out.println();

                                    int innerChoice3 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                                    switch (innerChoice3) {
                                        case 1 -> {
                                            appointmentService.cancelAppointment(selected);
                                            System.out.println("Wizyta została odwołana.");
                                            logService.addLog(new Log("appointment-cancel", String.format("%s anulował wizytę (%s).", accountService.getAccountById(loggedAccountUuid).getLogin(), selected.getUuid())));
                                            loop = false;
                                        }
                                        case 2 -> loop = false;
                                    }
                                }
                            }
                            case 2 -> loop = false;
                        }
                    }

                    break;
                }
            }
            case 2 -> {
                List<Appointment> appointmentList = appointmentService.getAllAppointments().stream()
                        .filter(appointment -> appointment.getDoctorId().equals(accountService.getAccountById(loggedAccountUuid).getUuid().toString()))
                        .filter(appointmentService::isFinished)
                        .toList();

                if (appointmentList.isEmpty()) {
                    System.out.println("Historia wizyt jest pusta.");
                    return;
                }

                for (Appointment appointment : appointmentList) {
                    String status;
                    switch (appointment.getStatus()) {
                        case Finished -> status = "Zakończona";
                        default -> status = "Anulowana";
                    }

                    System.out.printf("[%s] %s\n", status, appointmentService.getAppointmentString(appointment, Account.Type.Doctor));
                }
                System.out.println();
            }
            case 3 -> {
                List<Schedule> scheduleList = scheduleService.getDoctorSchedules(accountService.getAccountById(loggedAccountUuid));

                System.out.println("== Terminy ==");

                if (scheduleList.isEmpty()) {
                    System.out.println("Brak.");
                } else {
                    scheduleList.forEach(schedule -> System.out.printf("%s, %s - %s\n",
                            schedule.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("pl")),
                            schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    ));
                }

                boolean loop = true;
                while (loop) {
                    System.out.println();
                    System.out.println("[1] Dodaj nowy termin");
                    System.out.println("[2] Usuń istniejący termin");
                    System.out.println("[3] Powrót");

                    int innerChoice = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                    switch (innerChoice) {
                        case 1 -> {
                            DayOfWeek dayOfWeek = MenuUtil.getDayOfWeek(scanner, "Wybierz dzień tygodnia: ");
                            LocalTime from = MenuUtil.getLocalTime(scanner, "Podaj godzinę (HH:mm): ");
                            String officeAddress = MenuUtil.getString(scanner, "Podaj adres pokoju: ");
                            int officeNumber = MenuUtil.getInt(scanner, "Podaj numer pokoju: ");

                            Schedule schedule = new Schedule(
                                    UUID.randomUUID(),
                                    from,
                                    from.plusHours(1),
                                    dayOfWeek,
                                    new Office(officeNumber, officeAddress),
                                    loggedAccountUuid.toString(),
                                    true
                            );

                            scheduleService.addSchedule(schedule);
                            System.out.println("Dodano nowy termin.");
                            logService.addLog(new Log("schedule-add", String.format("%s dodał termin (%s).", accountService.getAccountById(loggedAccountUuid).getLogin(), schedule.getUuid())));
                            loop = false;
                        }
                        case 2 -> {
                            while (loop) {
                                Map<Integer, Schedule> scheduleMap = new LinkedHashMap<>();
                                Integer i = 1;

                                for (Schedule schedule : scheduleList) {
                                    scheduleMap.put(i, schedule);

                                    System.out.printf("[%d] %s, %s - %s\n", i,
                                            schedule.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("pl")),
                                            schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                                    );

                                    i++;
                                }
                                System.out.println("[0] Powrót");
                                System.out.println();

                                int innerChoice2 = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                                if (innerChoice2 == 0) {
                                    loop = false;
                                    break;
                                }

                                Schedule selectedSchedule = scheduleMap.get(innerChoice2);
                                if (selectedSchedule == null) {
                                    System.out.println("Niepoprawny wybór. Spróbuj ponownie.\n");
                                    continue;
                                }

                                scheduleService.delete_schedule(selectedSchedule);
                                System.out.println("Termin został usunięty.");
                                logService.addLog(new Log("schedule-remove", String.format("%s usunął termin (%s).", accountService.getAccountById(loggedAccountUuid).getLogin(), selectedSchedule.getUuid())));
                                loop = false;
                            }
                        }
                        case 3 -> loop = false;
                        default -> System.out.println("Niepoprawny wybór. Spróbuj ponownie.");
                    }
                }
            }
            case 4 -> {
                if (!changePassword(scanner))
                    return;
                System.out.println("Hasło zostało zmienione.");
                logService.addLog(new Log("password-change", String.format("%s zmienił swoje hasło.", accountService.getAccountById(loggedAccountUuid).getLogin())));
            }
            case 5 -> System.exit(0);
            default -> System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
        }
    }


    private void patientMenu(Scanner scanner) {
        System.out.println();
        System.out.println("[1] Wyświetl moje wizyty");
        System.out.println("[2] Wyświetl listę lekarzy");
        System.out.println("[3] Umów wizytę");
        System.out.println("[4] Wyświetl historię wizyt");
        System.out.println("[5] Zmień swoje hasło");
        System.out.println("[6] Wyjdź z aplikacji");
        System.out.println();

        int choice = MenuUtil.getInt(scanner, "Wybierz opcję: ");
        switch (choice) {
            case 1 -> {
                List<Appointment> appointmentList = appointmentService.getAllAppointments().stream()
                        .filter(appointment -> appointment.getPatientId().equals(accountService.getAccountById(loggedAccountUuid).getUuid().toString()))
                        .filter(appointment -> !appointmentService.isFinished(appointment))
                        .toList();

                if (appointmentList.isEmpty()) {
                    System.out.println("Brak umówionych wizyt.");
                    return;
                }

                Map<Integer, Appointment> appointedMap = new LinkedHashMap<>();

                boolean loop = true;
                while (loop) {
                    Integer i = 1;
                    for (Appointment appointment : appointmentList) {
                        appointedMap.put(i, appointment);

                        System.out.printf("[%d] %s\n", i, appointmentService.getAppointmentString(appointment, Account.Type.Patient));

                        i++;
                    }
                    System.out.println("[0] Powrót");
                    System.out.println();

                    int innerChoice = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                    if (innerChoice == 0) {
                        break;
                    }

                    Appointment selected = appointedMap.get(innerChoice);
                    if (selected == null) {
                        System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.\n");
                        continue;
                    }

                    System.out.println();
                    System.out.println(appointmentService.getAppointmentString(selected, Account.Type.Patient));

                    while (loop) {
                        System.out.println();
                        System.out.println("[1] Anuluj wizytę");
                        System.out.println("[2] Powrót");
                        System.out.println();

                        int innerChoice2 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                        switch (innerChoice2) {
                            case 1 -> {
                                if (!appointmentService.isEditable(selected)) {
                                    System.out.println("Tej wizyty nie można już anulować.");
                                    continue;
                                }

                                while (loop) {
                                    System.out.println();
                                    System.out.println("Czy na pewno chcesz anulować tę wizytę?");
                                    System.out.println();
                                    System.out.println("[1] Tak");
                                    System.out.println("[2] Nie");
                                    System.out.println();

                                    int innerChoice3 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                                    switch (innerChoice3) {
                                        case 1 -> {
                                            appointmentService.cancelAppointment(selected);
                                            System.out.println("Wizyta została anulowana.");
                                            logService.addLog(new Log("appointment-cancel", String.format("%s anulował wizytę (%s).", accountService.getAccountById(loggedAccountUuid).getLogin(), selected.getUuid())));
                                            loop = false;
                                        }
                                        case 2 -> loop = false;
                                    }
                                }
                            }
                            case 2 -> loop = false;
                        }
                    }

                    break;
                }
            }
            case 2 -> {
                List<Account> doctors = accountService.getAccountsByType(Account.Type.Doctor);
                if (doctors.isEmpty()) {
                    System.out.println("Brak lekarzy.");
                    return;
                }

                System.out.println("== Lista lekarzy ==");
                for (Account doctor : doctors) {
                    System.out.printf("%s %s\n", doctor.getName(), doctor.getSurname());
                }
            }
            case 3 -> {
                List<Account> doctors = accountService.getAccountsByType(Account.Type.Doctor);
                if (doctors.isEmpty()) {
                    System.out.println("Brak dostępnych lekarzy.");
                    return;
                }

                System.out.println("== Lista dostępnych lekarzy ==");
                boolean loop = true;
                while (loop) {
                    Map<Integer, Account> doctorMap = new LinkedHashMap<>();
                    Integer i = 1;
                    for (Account doctor : doctors) {
                        doctorMap.put(i, doctor);
                        System.out.printf("[%d] %s %s\n", i, doctor.getName(), doctor.getSurname());
                        i++;
                    }
                    System.out.println("[0] Powrót");
                    System.out.println();

                    int innerChoice = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                    if (innerChoice == 0) {
                        break;
                    }

                    Account selectedDoctor = doctorMap.get(innerChoice);
                    if (selectedDoctor == null) {
                        System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.\n");
                        continue;
                    }

                    System.out.println();

                    List<Schedule> scheduleList = scheduleService.getDoctorSchedules(selectedDoctor);
                    List<ScheduleWithDate> schedulesToReserve = new ArrayList<>();
                    int weekLimit = 2;

                    for (int k = 0; k < weekLimit; k++) {
                        for (Schedule schedule : scheduleList) {
                            ScheduleWithDate s = new ScheduleWithDate(
                                    schedule,
                                    LocalDate.now().with(TemporalAdjusters.next(schedule.getDayOfWeek())).plusWeeks(k)
                            );

                            schedulesToReserve.add(s);
                        }
                    }

                    schedulesToReserve = schedulesToReserve.stream()
                            .filter(scheduleWithDate -> !appointmentService.isScheduleAppointed(scheduleWithDate))
                            .toList();

                    System.out.println("== Lista dostępnych terminów ==");

                    while (loop) {
                        Map<Integer, ScheduleWithDate> scheduleMap = new LinkedHashMap<>();
                        Integer j = 1;
                        for (ScheduleWithDate schedule : schedulesToReserve) {
                            scheduleMap.put(j, schedule);

                            System.out.printf("[%d] %s, %s - %s\n", j,
                                    schedule.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    schedule.getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    schedule.getSchedule().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            );

                            j++;
                        }
                        System.out.println("[0] Powrót");
                        System.out.println();

                        int innerChoice2 = MenuUtil.getInt(scanner, "Wybierz opcję: ");

                        if (innerChoice2 == 0) {
                            return;
                        }

                        ScheduleWithDate selectedSchedule = scheduleMap.get(innerChoice2);
                        if (selectedSchedule == null) {
                            System.out.println("Niepoprawny wybór. Spróbuj ponownie.\n");
                            continue;
                        }

                        while (loop) {
                            System.out.println("\nPodsumowanie:");
                            System.out.println("Lekarz: dr " + selectedDoctor.getName() + " " + selectedDoctor.getSurname());
                            System.out.println("Godzina: " + selectedSchedule.getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                            System.out.println("Dzień: " + selectedSchedule.getDate());
                            System.out.println("Miejsce: " + selectedSchedule.getSchedule().getOffice().getAddress() + ", pokój nr " + selectedSchedule.getSchedule().getOffice().getNumber());
                            System.out.println();
                            System.out.println("[1] Umów wizytę");
                            System.out.println("[2] Anuluj");
                            System.out.println();

                            int innerChoice3 = MenuUtil.getInt(scanner, "Wybierz opcję: ");
                            switch (innerChoice3) {
                                case 1 -> {
                                    Appointment appointment = appointmentService.addAppointment(
                                            accountService.getAccountById(loggedAccountUuid),
                                            selectedDoctor,
                                            selectedSchedule.getSchedule(),
                                            selectedSchedule.getDate()
                                    );

                                    System.out.println("Wizyta została umówiona.");
                                    logService.addLog(new Log("appointment-add", String.format("%s umówił wizytę (%s).", accountService.getAccountById(loggedAccountUuid).getLogin(), appointment.getUuid().toString())));
                                    loop = false;
                                }
                                case 2 -> loop = false;
                            }
                        }
                    }
                }
            }
            case 4 -> {
                List<Appointment> appointmentList = appointmentService.getAllAppointments().stream()
                        .filter(appointment -> appointment.getPatientId().equals(accountService.getAccountById(loggedAccountUuid).getUuid().toString()))
                        .filter(appointmentService::isFinished)
                        .toList();

                if (appointmentList.isEmpty()) {
                    System.out.println("Historia wizyt jest pusta.");
                    return;
                }

                for (Appointment appointment : appointmentList) {
                    String status;
                    switch (appointment.getStatus()) {
                        case Finished -> status = "Zakończona";
                        default -> status = "Anulowana";
                    }

                    System.out.printf("[%s] %s\n", status, appointmentService.getAppointmentString(appointment, Account.Type.Patient));
                }
            }
            case 5 -> {
                if (!changePassword(scanner))
                    return;
                System.out.println("Hasło zostało zmienione.");
                logService.addLog(new Log("password-change", String.format("%s zmienił swoje hasło.", accountService.getAccountById(loggedAccountUuid).getLogin())));
            }
            case 6 -> System.exit(0);
            default -> System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
        }
    }

    private boolean changePassword(Scanner scanner) {
        String password = MenuUtil.getString(scanner, "Podaj nowe hasło: ");
        if (!PasswordService.checkPasswordRequirements(password))
            return false;
        String repeatedPassword = MenuUtil.getString(scanner, "Powtórz nowe hasło: ");

        if (!password.equals(repeatedPassword)) {
            System.out.println("Podane hasła nie są takie same.");
            return false;
        }

        String currentPassword = MenuUtil.getString(scanner, "Podaj aktualne hasło: ");
        if (!PasswordService.validatePassword(currentPassword, accountService.getAccountById(loggedAccountUuid).getPassword())) {
            System.out.println("Aktualne hasło nie jest poprawne.");
            return false;
        }

        passwordService.changePassword(accountService.getAccountById(loggedAccountUuid), password);
        return true;
    }
}
