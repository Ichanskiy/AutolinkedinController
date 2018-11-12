package tech.mangosoft.autolinkedin.controller;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
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
import tech.mangosoft.autolinkedin.controller.messages.GrabbingMessage;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.IGroupRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;
import tech.mangosoft.autolinkedin.service.LinkedInService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;
import static tech.mangosoft.autolinkedin.utils.JacksonUtils.getJson;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AssignmentControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String LOCATION = "Greater Test Area";
    private static final String INDUSTRIES = "Test Games";
    private static final String POSITION = "CEO";

    private MockMvc mockMvc;

    @Mock
    private IAssignmentRepository assignmentRepository;

    @Mock
    private IGroupRepository groupRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private LinkedInService linkedInService;

    @Mock
    private ILocationRepository locationRepository;

    @InjectMocks
    private AssignmentController assignmentController;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController).dispatchOptions(true).build();
    }

//    @Test
//    public void createGrabbingAssignmentValid() throws Exception{
//        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING;
//
//        GrabbingMessage message = new GrabbingMessage();
//        message.setLogin(EMAIL);
//        message.setFullLocationString(LOCATION);
//        message.setGroupId(1L);
//        message.setIndustries(INDUSTRIES);
//        message.setPosition(POSITION);
//        Account account = new Account();
//        account.setId(1L);
//        account.setUsername(message.getLogin());
//        account.setPassword("TEST");
//        account.setFirst("fdsf");
//        account.setLast("fsdfs");
//        account.setGrabbingLimit(100);
//        List<Long> ids = new ArrayList<>();
//        ids.add(1L);
//        message.setIdsHeadcount(ids);
//        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
//        when(linkedInService.createGrabbingAssignment(message, account)).thenReturn(any(Assignment.class));
//        MockHttpServletResponse response = mockMvc
//                .perform(post(request).content(getJson(message))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse();
//        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
//        verify(accountRepository, times(1)).getAccountByUsername(message.getLogin());
//        verify(linkedInService, times(1)).createGrabbingAssignment(message, account);
//        verifyNoMoreInteractions(accountRepository);
//        verifyNoMoreInteractions(linkedInService);
//    }
//
//    @Test
//    public void createGrabbingAssignmentInvalid() throws Exception{
//        String request = ASSIGNMENT_CONTROLLER + CREATE_GRABBING;
//        GrabbingMessage message = new GrabbingMessage();
//        message.setLogin(EMAIL);
//        message.setFullLocationString(LOCATION);
//        message.setGroupId(1L);
//        message.setIndustries(INDUSTRIES);
//        message.setPosition(POSITION);
//        List<Long> ids = new ArrayList<>();
//        ids.add(1L);
//        message.setIdsHeadcount(ids);
//        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(any(Account.class));
//        when(linkedInService.createGrabbingAssignment(message, any(Account.class))).thenReturn(any(Assignment.class));
//        MockHttpServletResponse response = mockMvc
//                .perform(post(request).content(getJson(message))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse();
//        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
//        verify(accountRepository, times(1)).getAccountByUsername(message.getLogin());
//        verify(linkedInService, times(1)).createGrabbingAssignment(message, any(Account.class));
//        verifyNoMoreInteractions(accountRepository);
//        verifyNoMoreInteractions(linkedInService);
//    }


    @Test
    public void getAssignmentByIdValid() throws Exception{
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
    public void getAssignmentByIdInvalid() throws Exception{
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
    public void getGroupsTest() throws Exception {
        String request = ASSIGNMENT_CONTROLLER + GET_GROUPS;
        when(groupRepository.findAll()).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request, 1L))
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(groupRepository, times(1)).findAll();
        verifyNoMoreInteractions(groupRepository);

    }
}
