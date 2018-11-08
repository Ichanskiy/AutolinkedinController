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

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.ADMIN_CONTROLLER;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.ALL_BY_PAGE;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.BY_ID;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {
    private static final String EMAIL = "test@email.com";

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private IAccountRepository accountRepository;

    @InjectMocks
    private AdminController adminController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).dispatchOptions(true).build();
    }

    @Test
    public void getAllAccountTestValid() throws Exception {
        String request = ADMIN_CONTROLLER + ALL_BY_PAGE;
        when(accountService.getAllAccounts(1)).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountService, times(1)).getAllAccounts(1);
        verify(accountRepository, times(1)).count();
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountService);
    }

    @Test
    public void getAllAccountTestInValid() throws Exception {
        String request = ADMIN_CONTROLLER + ALL_BY_PAGE;
        when(accountService.getAllAccounts(1)).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountService, times(1)).getAllAccounts(1);
        verify(accountRepository, times(0)).count();
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountByIdValid() throws Exception{
        String request = ADMIN_CONTROLLER + BY_ID;
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
        String request = ADMIN_CONTROLLER + BY_ID;
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

    @Test
    public void deleteAccountValid() throws Exception{
        String request = ADMIN_CONTROLLER;
        Account account = new Account();
        account.setUsername(EMAIL);
        when(accountRepository.getAccountByUsername(account.getUsername())).thenReturn(account);
        doNothing().when(accountService).delete(account);
        mockMvc.perform(delete(request).content(EMAIL)).andExpect(status().isOk()).andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(account.getUsername());
        verify(accountService, times(1)).delete(account);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountService);
    }

    @Test
    public void deleteAccountInvalid() throws Exception{
        String request = ADMIN_CONTROLLER;
        Account account = new Account();
        account.setUsername(EMAIL);
        when(accountRepository.getAccountByUsername(account.getUsername())).thenReturn(null);
        mockMvc.perform(delete(request).content(EMAIL)).andExpect(status().isBadRequest()).andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(account.getUsername());
        verifyNoMoreInteractions(accountRepository);
    }
}