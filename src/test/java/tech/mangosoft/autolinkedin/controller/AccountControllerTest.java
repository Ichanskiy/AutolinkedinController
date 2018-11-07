package tech.mangosoft.autolinkedin.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.service.AccountService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public void getAccountValid() throws Exception {
        String request = ControllerAPI.ACCOUNT_CONTROLLER + ControllerAPI.BY_ID;
        Account account = new Account("first", "last", "username", "pas", 100);
        account.setId(10L);
        when(accountRepository.getById(account.getId())).thenReturn(account);
        mockMvc.perform(get(request, account.getId())).andExpect(status().isOk()).andReturn();
//        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountInvalid() throws Exception {
        String request = ControllerAPI.ACCOUNT_CONTROLLER + ControllerAPI.BY_ID;
        when(accountRepository.getById(10L)).thenReturn(Mockito.any(Account.class));
        mockMvc.perform(get(request, 10L)).andExpect(status().isBadRequest()).andReturn();
    }

}