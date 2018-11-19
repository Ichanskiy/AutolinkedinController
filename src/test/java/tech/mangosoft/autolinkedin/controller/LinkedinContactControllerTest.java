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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.ProcessedContactMessage;
import tech.mangosoft.autolinkedin.controller.messages.UpdateContactMessage;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.filestorage.FileStorage;
import tech.mangosoft.autolinkedin.service.ContactService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;
import static tech.mangosoft.autolinkedin.utils.JacksonUtils.getJson;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class LinkedinContactControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String LOCATION = "Greater Test Area";
    private static final String INDUSTRIES = "Test Games";
    private static final String POSITION = "CEO";
    private static final Long ID = 1L;

    private MockMvc mockMvc;

    @Mock
    private IAssignmentRepository assignmentRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private ContactService contactService;

    @Mock
    private ILinkedInContactRepository contactRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private LinkedinContactController contactController;

    @BeforeEach
    void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(contactController).dispatchOptions(true).build();
    }

    @Test
    @DisplayName("Get contacts with valid params")
    void getContactsValidTest() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACTS;
        ContactsMessage message = new ContactsMessage();
        message.setIndustries(INDUSTRIES);
        message.setLocation(LOCATION);
        message.setPosition(POSITION);
        message.setPage(0);
        when(contactService.getContactsByParam(any(ContactsMessage.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
        MockHttpServletResponse response = mockMvc
                .perform(post(request)
                        .content(Objects.requireNonNull(getJson(message)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(contactService, times(1)).getContactsByParam(any(ContactsMessage.class));
        verifyNoMoreInteractions(contactService);
    }

    @Test
    @DisplayName("Get contact by id with valid params")
    void getContactByIdValid() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACT_BY_ID;
        when(contactRepository.getById(ID)).thenReturn(new LinkedInContact());
        MockHttpServletResponse response = mockMvc
                .perform(get(request, ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(contactRepository, times(1)).getById(ID);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    @DisplayName("Get contact by id with invalid params")
    void getContactByIdInvalid() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACT_BY_ID;
        when(contactRepository.getById(ID)).thenReturn(null);
        mockMvc.perform(get(request, ID))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(contactRepository, times(1)).getById(ID);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    @DisplayName("Update contact with valid params")
    void updateContactValid() throws Exception {
        String request = CONTACT_CONTROLLER + UPDATE_CONTACT;
        when(contactRepository.getById(ID)).thenReturn(new LinkedInContact());
        when(contactService.update(any(LinkedInContact.class), any(UpdateContactMessage.class))).thenReturn(new LinkedInContact());
        MockHttpServletResponse response = mockMvc
                .perform(put(request)
                        .param("id", "1")
                        .param("email", EMAIL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(contactRepository, times(1)).getById(ID);
        verify(contactService, times(1)).update(any(LinkedInContact.class), any(UpdateContactMessage.class));
        verifyNoMoreInteractions(contactRepository, contactService);
    }

    @Test
    @DisplayName("Update contact with invalid contact")
    void updateContactInvalidContact() throws Exception {
        String request = CONTACT_CONTROLLER + UPDATE_CONTACT;
        when(contactRepository.getById(ID)).thenReturn(null);
        mockMvc.perform(put(request)
                .param("id", "1")
                .param("email", EMAIL))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(contactRepository, times(1)).getById(ID);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    @DisplayName("Get contacts by status with valid params")
    void getContactsByStatusValid() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACTS_BY_STATUS;
        ProcessedContactMessage message = new ProcessedContactMessage();
        message.setLogin(EMAIL);
        message.setStatus(30);
        message.setAssignmentId(ID);
        message.setPage(1);
        Assignment assignment = new Assignment();
        Account account = new Account();

        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(assignmentRepository.getById(message.getAssignmentId())).thenReturn(assignment);
        when(contactService
                .getContactsByStatus(account, assignment, message.getStatus(), message.getPage(), 40))
                .thenReturn(new ArrayList<>());
        when(contactService.getCountContactsByStatus(account, assignment, message.getStatus())).thenReturn(10);
        MockHttpServletResponse response = mockMvc
                .perform(get(request).content(Objects.requireNonNull(getJson(message)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(accountRepository, times(1)).getAccountByUsername(message.getLogin());
        verify(assignmentRepository, times(1)).getById(message.getAssignmentId());
        verify(contactService, times(1))
                .getContactsByStatus(account, assignment, message.getStatus(), message.getPage(), 40);
        verify(contactService, times(1))
                .getCountContactsByStatus(account, assignment, message.getStatus());
        verifyNoMoreInteractions(accountRepository, assignmentRepository, contactService);
    }

    @Test
    @DisplayName("Get contacts by status with invalid account")
    void getContactsByStatusInvalidAccount() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACTS_BY_STATUS;
        ProcessedContactMessage message = new ProcessedContactMessage();
        message.setLogin(EMAIL);
        message.setStatus(30);
        message.setAssignmentId(ID);
        message.setPage(1);
        when(accountRepository.getAccountByUsername(EMAIL)).thenReturn(null);
        mockMvc.perform(get(request).content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(EMAIL);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    @DisplayName("Get contacts by status with invalid assignment")
    void getContactsByStatusInvalidAssignment() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACTS_BY_STATUS;
        ProcessedContactMessage message = new ProcessedContactMessage();
        message.setLogin(EMAIL);
        message.setAssignmentId(ID);
        message.setPage(1);
        Account account = new Account();

        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        when(assignmentRepository.getById(message.getAssignmentId())).thenReturn(null);
        mockMvc.perform(get(request).content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(message.getLogin());
        verify(assignmentRepository, times(1)).getById(message.getAssignmentId());
        verifyNoMoreInteractions(accountRepository, assignmentRepository);
    }

    @Test
    @DisplayName("Get contacts by status with invalid assignment id")
    void getContactsByStatusInvalidAssignmentId() throws Exception {
        String request = CONTACT_CONTROLLER + GET_CONTACTS_BY_STATUS;
        ProcessedContactMessage message = new ProcessedContactMessage();
        message.setLogin(EMAIL);
        message.setStatus(30);
        message.setAssignmentId(null);
        message.setPage(1);
        Account account = new Account();

        when(accountRepository.getAccountByUsername(message.getLogin())).thenReturn(account);
        mockMvc.perform(get(request).content(Objects.requireNonNull(getJson(message)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(accountRepository, times(1)).getAccountByUsername(message.getLogin());
        verifyNoMoreInteractions(accountRepository);
    }


    @Test
    @DisplayName("Get list files with valid params")
    void getListFilesValid() throws Exception {
        String request = CONTACT_CONTROLLER + ALL;
        ContactsMessage message = new ContactsMessage();
        message.setPosition(POSITION);
        message.setLocation(LOCATION);
        message.setIndustries(INDUSTRIES);
        message.setPage(1);
        doNothing().when(contactService).createCsvFileByParam(any(ContactsMessage.class));
        when(fileStorage.loadFiles()).thenReturn(Stream.empty());
        MockHttpServletResponse response = mockMvc
                .perform(post(request).content(Objects.requireNonNull(getJson(message)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(contactService, times(1)).createCsvFileByParam(any(ContactsMessage.class));
        verify(fileStorage, times(1)).loadFiles();
        verifyNoMoreInteractions(contactService, fileStorage);
    }

    @Test
    @DisplayName("Download file by filename with valid params")
    void downloadFileValid() throws Exception {
        String request = CONTACT_CONTROLLER + DOWNLOAD_FILE_BY_FILENAME;

        when(fileStorage.loadFile()).thenReturn(new ByteArrayResource(new byte[10]));
        MockHttpServletResponse response = mockMvc
                .perform(get(request, "contacts.csv"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(fileStorage, times(1)).loadFile();
        verifyNoMoreInteractions(fileStorage);
    }


    @Test
    @DisplayName("Upload file with valid params")
    void uploadFileValid() throws Exception {
        String request = CONTACT_CONTROLLER + UPLOAD_FILE;
        byte[] fileContent = "bar".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileContent);
        when(fileStorage.store(mockMultipartFile)).thenReturn(true);
        MockHttpServletResponse response = mockMvc
                .perform(multipart(request).file(mockMultipartFile))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(fileStorage, times(1)).store(mockMultipartFile);
        verifyNoMoreInteractions(fileStorage);
    }

    @Test
    @DisplayName("Upload file with invalid params")
    void uploadFileInvalid() throws Exception {
        String request = CONTACT_CONTROLLER + UPLOAD_FILE;
        byte[] fileContent = "bar".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileContent);
        when(fileStorage.store(mockMultipartFile)).thenReturn(false);
        MockHttpServletResponse response = mockMvc
                .perform(multipart(request).file(mockMultipartFile))
                .andExpect(status().is(500))
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(fileStorage, times(1)).store(mockMultipartFile);
        verifyNoMoreInteractions(fileStorage);
    }

}
