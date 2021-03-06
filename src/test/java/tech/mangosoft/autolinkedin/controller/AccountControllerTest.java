package tech.mangosoft.autolinkedin.controller;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Company;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.service.AccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;
import static tech.mangosoft.autolinkedin.utils.JacksonUtils.getJson;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AccountControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "qwe123";
    private static final String FIRST = "first";
    private static final String LAST = "last";
    private static final String COMPANY = "TestSoft";

    private  MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private IAccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).dispatchOptions(true).build();
    }

    @Test
    @DisplayName("Get account by userName and password")
    void getAccountValid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        when(accountRepository.getAccountByUsernameAndPasswordAndConfirmIsTrue(EMAIL, PASSWORD))
                .thenReturn(new Account());
        MockHttpServletResponse response = mockMvc
                .perform(get(request).param("login", EMAIL).param("password", PASSWORD))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsernameAndPasswordAndConfirmIsTrue(EMAIL, PASSWORD);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Get account by invalid userName and password")
    void getAccountInvalid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        when(accountRepository.getAccountByUsernameAndPasswordAndConfirmIsTrue(EMAIL, PASSWORD)).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request).param("login", EMAIL).param("password", PASSWORD))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsernameAndPasswordAndConfirmIsTrue(EMAIL, PASSWORD);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Get account by id")
    void getAccountByIdValid() throws Exception {
        String request = ACCOUNT_CONTROLLER + BY_ID;
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
    @DisplayName("Get account by invalid id")
    void getAccountByIdInvalid() throws Exception {
        String request = ACCOUNT_CONTROLLER + BY_ID;
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
    @DisplayName("Create account")
    void createAccountValid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setUsername(EMAIL);
        account.setPassword(PASSWORD);
        account.setFirst(FIRST);
        account.setLast(LAST);
        account.setGrabbingLimit(100);
        Company company = new Company().setName(COMPANY);
        account.setCompany(company);
        when(accountService.accountNotValid(any(Account.class))).thenReturn(false);
        when(accountService.createAccount(any(Account.class))).thenReturn(account);
        MockHttpServletResponse response = mockMvc
                .perform(post(request).content(getJson(account))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountService, times(1)).accountNotValid(any(Account.class));
        verify(accountService, times(1)).createAccount(any(Account.class));
        verifyNoMoreInteractions(accountService);
    }

    @Test
    @DisplayName("Create account by invalid id")
    void createAccountInvalid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setUsername(EMAIL);
        account.setPassword(PASSWORD);
        account.setFirst(FIRST);
        account.setLast(LAST);
        account.setGrabbingLimit(100);
        Company company = new Company().setName(COMPANY);
        account.setCompany(company);
        when(accountService.accountNotValid(any(Account.class))).thenReturn(true);
        MockHttpServletResponse response = mockMvc
                .perform(post(request).content(getJson(account))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountService, times(1)).accountNotValid(any(Account.class));
        verifyNoMoreInteractions(accountService);
    }

    @Test
    @DisplayName("Delete account")
    void deleteAccountValid() throws Exception{
        String request = ACCOUNT_CONTROLLER;
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
    @DisplayName("Create account by invalid id")
    public void deleteAccountInvalid() throws Exception{
        String request = ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setUsername(EMAIL);
        when(accountRepository.getAccountByUsername(account.getUsername())).thenReturn(null);
        mockMvc.perform(delete(request).content(EMAIL)).andExpect(status().isBadRequest()).andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(account.getUsername());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Update password")
    void updatePasswordValid() throws Exception {
        String request = ACCOUNT_CONTROLLER + BY_PASSWORD;
        when(accountService.updatePasswordSuccesses(anyString(), anyString(), anyString())).thenReturn(true);
        mockMvc.perform(put(request)
                .param("username", EMAIL)
                .param("oldPassword", PASSWORD)
                .param("newPassword", PASSWORD))
                .andExpect(status().isOk())
                .andReturn();
        verify(accountService, times(1)).updatePasswordSuccesses(EMAIL, PASSWORD, PASSWORD);
        verifyNoMoreInteractions(accountService);
    }

    @Test
    @DisplayName("Update password invalid")
    void updatePasswordInvalid() throws Exception {
        String request = ACCOUNT_CONTROLLER + BY_PASSWORD;
        when(accountService.updatePasswordSuccesses(EMAIL, PASSWORD, PASSWORD)).thenReturn(false);
        mockMvc.perform(put(request)
                .param("username", EMAIL)
                .param("oldPassword", PASSWORD)
                .param("newPassword", PASSWORD))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(accountService, times(1)).updatePasswordSuccesses(EMAIL, PASSWORD, PASSWORD);
        verifyNoMoreInteractions(accountService);
    }

    @Test
    @DisplayName("Update account")
    void updateAccountValid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setId(1L);
        account.setUsername(EMAIL);
        account.setFirst(FIRST);
        account.setLast(LAST);
        account.setGrabbingLimit(100);
        Company company = new Company().setName(COMPANY);
        account.setCompany(company);
        when(accountRepository.getById(account.getId())).thenReturn(account);
        when(accountService.update(any(Account.class))).thenReturn(account);
        MockHttpServletResponse response = mockMvc
                .perform(put(request)
                        .param("id", String.valueOf(account.getId()))
                        .param("username", account.getUsername())
                        .param("first", account.getFirst())
                        .param("last", account.getLast())
                        .param("grabbingLimit", String.valueOf(account.getGrabbingLimit()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getById(account.getId());
        verify(accountService, times(1)).update(any(Account.class));
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountService);
    }

    @Test
    @DisplayName("Update account invalid")
    void updateAccountInvalid() throws Exception {
        String request = ACCOUNT_CONTROLLER;
        Account account = new Account();
        account.setId(1L);
        account.setUsername(EMAIL);
        account.setFirst(FIRST);
        account.setLast(LAST);
        account.setGrabbingLimit(100);
        Company company = new Company().setName(COMPANY);
        account.setCompany(company);
        when(accountRepository.getById(account.getId())).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(put(request)
                        .param("id", String.valueOf(account.getId()))
                        .param("username", account.getUsername())
                        .param("first", account.getFirst())
                        .param("last", account.getLast())
                        .param("grabbingLimit", String.valueOf(account.getGrabbingLimit()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getById(account.getId());
        verifyNoMoreInteractions(accountRepository);
    }
}