package org.sap.interfaces;

import org.sap.models.Account;

public interface IAuthorization {
    boolean register(String login, String password, String name, String surname);
    Account login(String login, String password);
}
