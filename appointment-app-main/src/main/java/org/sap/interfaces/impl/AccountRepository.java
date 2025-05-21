package org.sap.interfaces.impl;

import org.sap.interfaces.IAccountRepository;
import org.sap.models.Account;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountRepository implements IAccountRepository {
    private final String FILE_NAME = "accounts.csv";
    private final List<Account> accounts;

    public AccountRepository() {
        this.accounts = new ArrayList<>();
        load();
    }

    public List<Account> getAccounts() {
        return accounts.stream().map(Account::copy).collect(Collectors.toList());
    }

    @Override
    public Account getAccountById(UUID uuid) {
        return accounts.stream().filter(account -> account.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public Account getAccountByLogin(String login) {
        return accounts.stream().filter(account -> account.getLogin().equals(login)).findFirst().orElse(null);
    }

    @Override
    public Account addAccount(Account account) {
        if (getAccountById(account.getUuid()) != null || getAccountByLogin(account.getLogin()) != null)
            throw new IllegalArgumentException("Account already exists");

        Account newAccount = Account.copy(account);
        accounts.add(newAccount);
        save();

        return newAccount;
    }

    @Override
    public void editAccount(Account account) {
        accounts.stream().filter(a -> a.getUuid().equals(account.getUuid())).findFirst().ifPresent(accountToEdit -> accounts.set(accounts.indexOf(accountToEdit), account));
        save();
    }

    @Override
    public void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'", e);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");

                if (fields.length != 6)
                    throw new IOException();

                UUID uuid = UUID.fromString(fields[0]);
                Account.Type type = Account.Type.valueOf(fields[1]);
                String login = fields[2];
                String password = fields[3];
                String name = fields[4];
                String surname = fields[5];

                Account account = new Account(uuid, login, password, name, surname, type);
                accounts.add(account);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read line in file '" + FILE_NAME + "'", e);
        }
    }

    @Override
    public void save() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create file '" + FILE_NAME + "'");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Account account : accounts) {
                writer.write(account.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while saving accounts", e);
        }
    }
}
