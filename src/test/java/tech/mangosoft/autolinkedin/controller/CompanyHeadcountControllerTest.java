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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.mangosoft.autolinkedin.db.repository.ICompanyHeadcountRepository;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.ALL;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.HEADCOUNT_CONTROLLER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CompanyHeadcountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICompanyHeadcountRepository headcountRepository;

    @InjectMocks
    private CompanyHeadcountController companyHeadcountController;

    @BeforeEach
    void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(companyHeadcountController).dispatchOptions(true).build();
    }

    @Test
    @DisplayName("Get all company headcount")
    void getAllCompanyHeadcountValid() throws Exception {
        String request = HEADCOUNT_CONTROLLER + ALL;
        when(headcountRepository.findAll()).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(headcountRepository, times(1)).findAll();
        verifyNoMoreInteractions(headcountRepository);
    }

    @Test
    @DisplayName("Get all company headcount invalid")
    void getAllCompanyHeadcountInvalid() throws Exception {
        String request = HEADCOUNT_CONTROLLER + ALL;
        when(headcountRepository.findAll()).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(headcountRepository, times(1)).findAll();
        verifyNoMoreInteractions(headcountRepository);
    }
}
