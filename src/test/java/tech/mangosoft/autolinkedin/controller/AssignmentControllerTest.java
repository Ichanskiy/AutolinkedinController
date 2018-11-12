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
import tech.mangosoft.autolinkedin.controller.messages.ConnectionMessage;
import tech.mangosoft.autolinkedin.controller.messages.GrabbingMessage;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.IGroupRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;
import tech.mangosoft.autolinkedin.service.LinkedInService;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;
import static tech.mangosoft.autolinkedin.utils.JacksonUtils.getJson;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "pass";
    private static final String LOCATION = "Greater Test Area";
    private static final String INDUSTRIES = "Test Games";
    private static final String POSITION = "CEO";
    private static final Long ID = 1L;

    private MockMvc mockMvc;

    @Mock
    private IAssignmentRepository assignmentRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private ILocationRepository locationRepository;

    @Mock
    private LinkedInService linkedInService;

    @InjectMocks
    private AssignmentController assignmentController;

    @BeforeEach
    void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController).dispatchOptions(true).build();
    }

    @Test
    @DisplayName("Create grabbing assignment with valid param")
    void createGrabbingAssignmentValidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING;
        GrabbingMessage message = getGrabbingMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createGrabbingAssignment(any(GrabbingMessage.class), any(Account.class)))
                .thenReturn(new Assignment());
        MockHttpServletResponse response = mockMvc
                .perform(post(request)
                        .content(Objects.requireNonNull(getJson(message)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createGrabbingAssignment(any(GrabbingMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Create grabbing assignment with invalid account")
    void createGrabbingAssignmentAccountInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING;
        GrabbingMessage message = getGrabbingMessage();
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(null);
        mockMvc.perform(post(request)
                .content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Create grabbing assignment with invalid assignment")
    void createGrabbingAssignmentAssignmentInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING;
        GrabbingMessage message = getGrabbingMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createGrabbingAssignment(any(GrabbingMessage.class), any(Account.class)))
                .thenReturn(null);
        mockMvc.perform(post(request)
                .content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createGrabbingAssignment(any(GrabbingMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }


    @Test
    @DisplayName("Get assignment by id")
    void getAssignmentByIdValidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + BY_ID;
        when(assignmentRepository.getById(1L)).thenReturn(new Assignment());
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(assignmentRepository, times(1)).getById(1L);
        verifyNoMoreInteractions(assignmentRepository);

    }

    @Test
    @DisplayName("Get assignment by invalid id")
    void getAssignmentByIdInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + BY_ID;
        when(assignmentRepository.getById(1L)).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1L))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(assignmentRepository, times(1)).getById(1L);
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    @DisplayName("Get groups")
    void getGroupsTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + GET_GROUPS;
        when(groupRepository.findAll()).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(groupRepository, times(1)).findAll();
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    @DisplayName("Get locations")
    void getLocationsTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + GET_LOCATIONS;
        when(locationRepository.findAll()).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(locationRepository, times(1)).findAll();
        verifyNoMoreInteractions(locationRepository);

    }

    @Test
    @DisplayName("Delete assignment valid")
    void deleteAssignmentValid() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + BY_ID;
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        when(assignmentRepository.getById(assignment.getId())).thenReturn(assignment);
        doNothing().when(linkedInService).deleteAssignmentById(assignment.getId());
        MockHttpServletResponse response = mockMvc
                .perform(delete(request, 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        verify(assignmentRepository, times(1)).getById(assignment.getId());
        verify(linkedInService, times(1)).deleteAssignmentById(assignment.getId());
        verifyNoMoreInteractions(assignmentRepository, linkedInService);
    }

    @Test
    @DisplayName("Delete assignment invalid")
    void deleteAssignmentInvalid() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + BY_ID;
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        when(assignmentRepository.getById(assignment.getId())).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(delete(request, 1L))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(assignmentRepository, times(1)).getById(assignment.getId());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    @DisplayName("Change assignment status valid")
    void changeAssignmentStatusValid() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CHANGE_STATUS;
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setStatus(Status.STATUS_NEW);
        when(assignmentRepository.getById(assignment.getId())).thenReturn(assignment);
        doNothing().when(linkedInService).changeStatus(assignment.getId(), assignment.getStatus().getId());
        mockMvc.perform(put(request).param("id", "1").param("status", "0"))
                .andExpect(status().isOk())
                .andReturn();
        verify(assignmentRepository, times(1)).getById(assignment.getId());
        verify(linkedInService, times(1)).changeStatus(assignment.getId(), assignment.getStatus().getId());
        verifyNoMoreInteractions(assignmentRepository, linkedInService);
    }

    @Test
    @DisplayName("Change assignment status invalid")
    void changeAssignmentStatusInvalid() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CHANGE_STATUS;
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setStatus(Status.STATUS_NEW);
        when(assignmentRepository.getById(assignment.getId())).thenReturn(null);
        mockMvc.perform(put(request).param("id", "1").param("status", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(assignmentRepository, times(1)).getById(assignment.getId());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    @DisplayName("Create grabbing assignment sales with valid param")
    void createGrabbingSalesAssignmentValidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING_SALES;
        GrabbingMessage message = getGrabbingMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createGrabbingSalesAssignment(any(GrabbingMessage.class), any(Account.class)))
                .thenReturn(new Assignment());
        MockHttpServletResponse response = mockMvc
                .perform(post(request)
                        .content(Objects.requireNonNull(getJson(message)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createGrabbingSalesAssignment(any(GrabbingMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Create grabbing assignment sales with invalid account")
    void createGrabbingSalesAssignmentAccountInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING_SALES;
        GrabbingMessage message = getGrabbingMessage();
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(null);
        mockMvc.perform(post(request)
                .content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Create grabbing sales assignment with invalid assignment")
    void createGrabbingSalesAssignmentAssignmentInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING_SALES;
        GrabbingMessage message = getGrabbingMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createGrabbingSalesAssignment(any(GrabbingMessage.class), any(Account.class)))
                .thenReturn(null);
        mockMvc.perform(post(request)
                .content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createGrabbingSalesAssignment(any(GrabbingMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Create connection assignment with valid param")
    void createConnectionAssignmentValidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_CONNECTION;
        ConnectionMessage message = getConnectionMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createConnectionAssignment(any(ConnectionMessage.class), any(Account.class)))
                .thenReturn(new Assignment());
        MockHttpServletResponse response = mockMvc
                .perform(post(request).param("login", EMAIL)
                        .param("fullLocationString", LOCATION)
                        .param("position", POSITION)
                        .param("executionLimit", "100")
                        .param("industries", INDUSTRIES))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createConnectionAssignment(any(ConnectionMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Create connection assignment with invalid account")
    void createConnectionAssignmentAccountInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_CONNECTION;
        ConnectionMessage message = getConnectionMessage();
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(null);
        mockMvc.perform(post(request)
                .param("login", EMAIL)
                .param("fullLocationString", LOCATION)
                .param("position", POSITION)
                .param("executionLimit", "100")
                .param("industries", INDUSTRIES))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Create connection assignment with invalid assignment")
    void createConnectionAssignmentAssignmentInvalidTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + CREATE_CONNECTION;
        ConnectionMessage message = getConnectionMessage();
        Account account = getAccount(message);
        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(linkedInService.createConnectionAssignment(any(ConnectionMessage.class), any(Account.class)))
                .thenReturn(null);
        mockMvc.perform(post(request)
                .param("login", EMAIL)
                .param("fullLocationString", LOCATION)
                .param("position", POSITION)
                .param("executionLimit", "100")
                .param("industries", INDUSTRIES))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        verify(accountRepository, times(1))
                .getAccountByUsername(message.getLogin());
        verify(linkedInService, times(1))
                .createConnectionAssignment(any(ConnectionMessage.class), any(Account.class));
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Get statistics with valid params")
    void getStatisticsValidTest() throws Exception{
        String request = ASSIGNMENT_CONTROLLER + GET_STATISTICS;
        Account account = new Account();
        account.setId(ID);
        account.setUsername(EMAIL);
        account.setPassword(PASSWORD);
        account.setGrabbingLimit(100);
        when(accountRepository.getAccountByUsername(EMAIL)).thenReturn(account);
        when(linkedInService.getCountAssignment(account)).thenReturn(1);
        when(linkedInService.getStatistics(account, 1, 20)).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request)
                        .param("email", EMAIL)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getAccountByUsername(EMAIL);
        verify(linkedInService, times(1)).getCountAssignment(account);
        verify(linkedInService, times(1)).getStatistics(account, 1, 20);
        verifyNoMoreInteractions(accountRepository, linkedInService);
    }

    @Test
    @DisplayName("Get statistics with invalid account param")
    void getStatisticsInvalidAccountTest() throws Exception{
        String request = ASSIGNMENT_CONTROLLER + GET_STATISTICS;
        when(accountRepository.getAccountByUsername(EMAIL)).thenReturn(null);
        mockMvc
                .perform(get(request)
                        .param("email", EMAIL)
                        .param("page", "1"))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(EMAIL);
        verifyNoMoreInteractions(accountRepository);
    }

    private GrabbingMessage getGrabbingMessage() {
        GrabbingMessage message = new GrabbingMessage();
        message.setLogin(EMAIL);
        message.setFullLocationString(LOCATION);
        message.setIndustries(INDUSTRIES);
        message.setPosition(POSITION);
        return message;
    }

    private ConnectionMessage getConnectionMessage() {
        ConnectionMessage message = new ConnectionMessage();
        message.setLogin(EMAIL);
        message.setFullLocationString(LOCATION);
        message.setIndustries(INDUSTRIES);
        message.setPosition(POSITION);
        message.setExecutionLimit(100);
        return message;
    }

    private Account getAccount(GrabbingMessage message) {
        Account account = new Account();
        account.setId(ID);
        account.setUsername(message.getLogin());
        account.setPassword(PASSWORD);
        account.setGrabbingLimit(100);
        return account;
    }

    private Account getAccount(ConnectionMessage message) {
        Account account = new Account();
        account.setId(ID);
        account.setUsername(message.getLogin());
        account.setPassword(PASSWORD);
        account.setGrabbingLimit(100);
        return account;
    }

}
