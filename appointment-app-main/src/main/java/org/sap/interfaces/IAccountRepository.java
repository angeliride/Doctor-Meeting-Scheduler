package org.sap.interfaces;

import org.sap.models.Account;

import java.util.List;
import java.util.UUID;

public interface IAccountRepository {
	List<Account> getAccounts();

	Account getAccountById(UUID uuid);

	Account getAccountByLogin(String login);

	Account addAccount(Account account);

	void editAccount(Account account);

	void load();

	void save();
}
