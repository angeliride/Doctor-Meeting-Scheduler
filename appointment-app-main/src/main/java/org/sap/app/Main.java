package org.sap.app;

import org.sap.interfaces.*;
import org.sap.interfaces.impl.*;
import org.sap.services.*;

public class Main {
    public static void main(String[] args) {
        ILogRepository logRepository = new LogRepository();
        IAccountRepository accountRepository = new AccountRepository();
        IScheduleRepository scheduleRepository = new ScheduleRepository();
        IAppointmentRepository appointmentRepository = new AppointmentRepository(scheduleRepository);

        AccountService accountService = new AccountService(accountRepository);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, scheduleRepository, accountRepository);
        ScheduleService scheduleService = new ScheduleService(scheduleRepository);
        LogService logService = new LogService(logRepository);
        PasswordService passwordService = new PasswordService(accountRepository);

        IAuthorization authorization = new StandardAuthorization(accountRepository);

        App app = new App(authorization, accountService, appointmentService, scheduleService, logService, passwordService);
        app.run();
    }
}