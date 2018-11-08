package tech.mangosoft.autolinkedin.controller;

import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.service.AccountService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.utils.JacksonUtils.getJson;

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
        String request = ControllerAPI.ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setUsername("test@email.com");
        account.setPassword("qwe123");
        when(accountRepository.getAccountByUsernameAndPassword(account.getUsername(), account.getPassword()))
                .thenReturn(new Account());
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsernameAndPassword(account.getUsername(), account.getPassword());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountInvalid() throws Exception {
        String request = ControllerAPI.ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setUsername("test@email.com");
        account.setPassword("qwe123");
        when(accountRepository.getAccountByUsernameAndPassword(account.getUsername(), account.getPassword())).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request, account.getUsername(), account.getPassword()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsernameAndPassword(account.getUsername(), account.getPassword());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountByIdValid() throws Exception {
        String request = ControllerAPI.ACCOUNT_CONTROLLER + ControllerAPI.BY_ID;
        when(accountRepository.getById(1L)).thenReturn(new Account());
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getById(1L);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountByIdInvalid() throws Exception {
        String request = ControllerAPI.ACCOUNT_CONTROLLER + ControllerAPI.BY_ID;
        when(accountRepository.getById(1L)).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1L))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getById(1L);
        verifyNoMoreInteractions(accountRepository);
    }

}