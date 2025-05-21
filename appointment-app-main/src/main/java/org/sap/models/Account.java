package org.sap.models;

import java.util.UUID;

public class Account {
	public enum Type {
		Patient, Doctor, Administrator
	}

	private final UUID uuid;
	private final String login;
	private String password;
	private String name;
	private String surname;
	private Type type;

	public Account(UUID uuid, String login, String password, String name, String surname, Type type) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.uuid = uuid;
		this.type = type;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public static Account copy(Account account) {
		return new Account(account.getUuid(), account.getLogin(), account.getPassword(), account.getName(), account.getSurname(), account.getType());
	}

	public String toCsv() {
		return String.format("%s;%s;%s;%s;%s;%s", uuid, type.toString(), login, password, name, surname);
	}
}
