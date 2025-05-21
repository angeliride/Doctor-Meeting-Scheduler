package org.sap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sap.interfaces.IAccountRepository;
import org.sap.models.Account;
import org.sap.services.PasswordService;

import java.util.List;
import java.util.UUID;

public class PasswordServiceTest {
    static class AccountRepositoryMock implements IAccountRepository {

        @Override
        public List<Account> getAccounts() {
            return List.of();
        }

        @Override
        public Account getAccountById(UUID uuid) {
            return null;
        }

        @Override
        public Account getAccountByLogin(String login) {
            return null;
        }

        @Override
        public Account addAccount(Account account) {
            return null;
        }

        @Override
        public void editAccount(Account account) {

        }

        @Override
        public void load() {

        }

        @Override
        public void save() {

        }
    }

    IAccountRepository accountRepository;
    PasswordService passwordService;

    @BeforeEach
    public void setUp() {
        accountRepository = new AccountRepositoryMock();
        passwordService = new PasswordService(accountRepository);
    }

    @Test
    void testCheckPasswordRequirements() {
        Assertions.assertTrue(PasswordService.checkPasswordRequirements("Abcdef1!")); // ok
        Assertions.assertFalse(PasswordService.checkPasswordRequirements("")); // puste
        Assertions.assertFalse(PasswordService.checkPasswordRequirements("short1!")); // za krótkie
        Assertions.assertFalse(PasswordService.checkPasswordRequirements("abcdefg1!")); // brak wielkiej litery
        Assertions.assertFalse(PasswordService.checkPasswordRequirements("ABCDEFG!")); // brak cyfry
        Assertions.assertFalse(PasswordService.checkPasswordRequirements("Abcdefg1")); // brak znaku specjalnego
    }

    @Test
    void testHashPasswordAndValidatePassword() {
        String password = "Haslo123!";
        String hashed = PasswordService.toHashed(password);

        Assertions.assertNotNull(hashed);
        Assertions.assertTrue(PasswordService.validatePassword(password, hashed));
        Assertions.assertFalse(PasswordService.validatePassword("Haslo1234!", hashed));
    }
}
