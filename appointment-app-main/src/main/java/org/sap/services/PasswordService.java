package org.sap.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.sap.interfaces.IAccountRepository;
import org.sap.models.Account;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.stream.Collectors;

public class PasswordService {
	IAccountRepository accountRepository;
	private static final String ALL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final SecureRandom random = new SecureRandom();

	public PasswordService(IAccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public void resetPassword(Account account) {
		String randomPassword = random.ints(8, 0, ALL_CHARACTERS.length())
				.mapToObj(ALL_CHARACTERS::charAt)
				.map(Objects::toString)
				.collect(Collectors.joining());

		account.setPassword(toHashed(randomPassword));
		accountRepository.editAccount(account);
		System.out.println("Hasło zostało zresetowane. Nowe hasło: " + randomPassword);
	}

	public void changePassword(Account account, String password) {
		account.setPassword(toHashed(password));
		accountRepository.editAccount(account);
	}

	public static String toHashed(String str) {
		return BCrypt.withDefaults().hashToString(12, str.toCharArray());
	}

	public static boolean validatePassword(String password, String hashed) {
		return BCrypt.verifyer().verify(password.toCharArray(), hashed).verified;
	}

	public static boolean checkPasswordRequirements(String password) {
		if (password.isEmpty()) {
			System.out.println("Hasło nie może być puste.");
			return false;
		} else if (password.length() < 8) {
			System.out.println("Hasło musi mieć co najmniej 8 znaków.");
			return false;
		} else if (!password.matches(".*[A-Z].*")) {
			System.out.println("Hasło musi zawierać min. 1 wielką literę.");
			return false;
		} else if (!password.matches(".*\\d.*")) {
			System.out.println("Hasło musi zawierać min. 1 znak specjalny.");
			return false;
		} else if (!password.matches(".*[^a-zA-Z0-9].*")) {
			System.out.println("Hasło musi zawierać min. 1 cyfrę.");
			return false;
		}

		return true;
	}
}
