package tech.mangosoft.autolinkedin.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.Company;
import tech.mangosoft.autolinkedin.db.entity.enums.Role;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ICompanyRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.application.properties")
class AccountServiceTest {

    private static final String USER_NAME_GLOBAL = "AccountServiceTest@gmail.com";
    private static final String USER_NAME_LOCAL = "local@gmail.com";
    private static final String FAILED_USER_NAME = "failed";
    private static final String FIRST = "first";
    private static final String NEW_FIRST = "newFirst";
    private static final String LAST = "last";
    private static final String NEW_LAST = "newLast";
    private static final String PASSWORD = "password";
    private static final String NEW_PASSWORD_FIRST = "newPasswordFirst";
    private static final String NEW_PASSWORD_SECOND = "newPasswordSecond";
    private static final String COMPANY_NAME = "TestCompany";
    private static final String NEW_COMPANY_NAME = "NewTestCompany";

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private ICompanyRepository companyRepository;

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private AccountService accountService;

    private Account account;
    private Company company;
    private Company newCompany;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(new Company().setName(COMPANY_NAME));
        newCompany = companyRepository.save(new Company().setName(NEW_COMPANY_NAME));
        account = accountRepository.save(getAccount(USER_NAME_GLOBAL));
    }

    @AfterEach
    void tearDown() {
        accountRepository.delete(account);
        companyRepository.delete(companyRepository.getByName(company.getName()));
        companyRepository.delete(companyRepository.getByName(newCompany.getName()));
    }

    @Test
    @DisplayName("Update account")
    void updateTest() {
        account.setCompany(newCompany);
        account.setFirst(NEW_FIRST);
        account.setLast(NEW_LAST);
        account.setRole(Role.ADMIN);
        Account accountUpdated = accountService.update(account);
        assertEquals(accountUpdated.getId(), account.getId());
        assertEquals(accountUpdated.getUsername(), account.getUsername());
        assertEquals(accountUpdated.getPassword(), account.getPassword());
        assertEquals(accountUpdated.getRole(), account.getRole());
        assertEquals(accountUpdated.getLast(), account.getLast());
        assertEquals(accountUpdated.getFirst(), account.getFirst());
        assertEquals(accountUpdated.getPassword(), account.getPassword());
        assertEquals(accountUpdated.getCompany().getId(), account.getCompany().getId());
    }

    @DisplayName("Update password")
    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @CsvSource({FAILED_USER_NAME + "," + PASSWORD + "," + NEW_PASSWORD_FIRST,
            USER_NAME_GLOBAL + "," + NEW_PASSWORD_FIRST + "," + NEW_PASSWORD_SECOND})
    void updatePasswordSuccessesTest(String username, String oldPassword, String newPassword) {
        boolean updatePasswordIsSuccesses = accountService.updatePasswordSuccesses(username, oldPassword, newPassword);
        assertFalse(updatePasswordIsSuccesses);
    }

    @DisplayName("Update password failed")
    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @CsvSource({USER_NAME_GLOBAL + "," + PASSWORD + "," + NEW_PASSWORD_FIRST,
            USER_NAME_GLOBAL + "," + PASSWORD + "," + NEW_PASSWORD_SECOND})
    void updatePasswordFailedTest(String username, String oldPassword, String newPassword) {
        boolean updatePasswordIsSuccesses = accountService.updatePasswordSuccesses(username, oldPassword, newPassword);
        Account accountWithNewPass = accountRepository.getAccountByUsername(username);
        assertTrue(updatePasswordIsSuccesses);
        assertEquals(accountWithNewPass.getPassword(), newPassword);
    }


    @Test
    @DisplayName("Update password failed")
    void deleteTest() {
        Account account = new Account();
        account.setUsername(USER_NAME_LOCAL);
        accountRepository.save(account);
        Assignment assignment = assignmentRepository.save(new Assignment()
                .setTask(Task.TASK_GRABBING)
                .setAccount(account));
        Account accountDB = accountRepository.getAccountByUsername(account.getUsername());
        accountService.delete(accountDB);
        assertNull(accountRepository.getAccountByUsername(accountDB.getUsername()));
        assertNull(accountRepository.getById(accountDB.getId()));
        assignmentRepository.delete(assignmentRepository.getById(assignment.getId()));
    }

    @Test
    @DisplayName("Get all accounts")
    void getAllAccountsTest() {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            accounts.add(accountRepository.save(getAccount(USER_NAME_GLOBAL.concat(String.valueOf(i)))));
        }
        List<Account> accountsDB = accountService.getAllAccounts(1);
        assertEquals(40, accountsDB.size());
        accountRepository.deleteAll(accounts);
    }

    private Account getAccount(String userName) {
        Account account = new Account();
        account.setUsername(userName);
        account.setFirst(FIRST);
        account.setLast(LAST);
        account.setPassword(PASSWORD);
        account.setRole(Role.USER);
        account.setCompany(company);
        return account;
    }
}