package org.sap.services;

import org.sap.interfaces.IAccountRepository;
import org.sap.models.Account;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountService {
	private final IAccountRepository accountRepository;

	public AccountService(IAccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public List<Account> getAllAccounts() {
		return accountRepository.getAccounts();
	}

	public Account getAccountById(UUID id) {
		return accountRepository.getAccountById(id);
	}

	public List<Account> getAccountsByType(Account.Type type) {
		return getAllAccounts().stream().filter(account -> account.getType() == type).collect(Collectors.toList());
	}

	public void editAccount(Account account) {
		accountRepository.editAccount(account);
	}
}
