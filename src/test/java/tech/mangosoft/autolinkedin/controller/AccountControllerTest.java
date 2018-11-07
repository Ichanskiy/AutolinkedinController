package tech.mangosoft.autolinkedin.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.service.AccountService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private IAccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).dispatchOptions(true).build();
    }

    @Test
    public void getAccount() throws Exception {
        String request = "account/{id}";
        Account account = new Account("first", "last", "username", "pas", 100);
        when(accountRepository.getAccountByUsernameAndPassword(account.getUsername(), account.getPassword())).thenReturn(account);
        mockMvc.perform(get(request, account.getUsername(), account.getPassword())).andReturn();
        verifyNoMoreInteractions(accountRepository);
    }

}