package org.sap.interfaces.impl;

import org.sap.interfaces.IAccountRepository;
import org.sap.interfaces.IAuthorization;
import org.sap.models.Account;

import java.util.UUID;

import static org.sap.services.PasswordService.*;

public class StandardAuthorization implements IAuthorization {
	private final IAccountRepository accountRepository;

	public StandardAuthorization(IAccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public boolean register(String login, String password, String name, String surname) {
		if (accountRepository.getAccountByLogin(login) != null) {
			System.out.println("Ten login już istnieje.");
			return false;
		} else if (!checkPasswordRequirements(password)) {
			return false;
		}

		Account account	= new Account(
				UUID.randomUUID(),
				login, toHashed(password),
				Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(),
				Character.toUpperCase(surname.charAt(0)) + surname.substring(1).toLowerCase(),
				Account.Type.Patient
		);

		Account registered = accountRepository.addAccount(account);
		if (registered != null) {
			return true;
		} else {
			System.out.println("Wystąpił błąd.");
			return false;
		}
	}

	public Account login(String login, String password) {
		Account account = accountRepository.getAccountByLogin(login);
		if (account == null) {
			System.out.println("Nie znaleziono konta o podanym loginie.");
			return null;
		}

		if (validatePassword(password, account.getPassword())) {
			return account;
		} else {
			System.out.println("Podano nieprawidłowe dane logowania.");
			return null;
		}
	}
}
