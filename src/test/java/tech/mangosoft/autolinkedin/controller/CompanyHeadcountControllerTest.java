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
import tech.mangosoft.autolinkedin.db.repository.ICompanyHeadcountRepository;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.ALL;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.HEADCOUNT_CONTROLLER;

@RunWith(MockitoJUnitRunner.class)
public class CompanyHeadcountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICompanyHeadcountRepository headcountRepository;

    @InjectMocks
    private CompanyHeadcountController companyHeadcountController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(companyHeadcountController).dispatchOptions(true).build();
    }

    @Test
    public void getAllCompanyHeadcountValid() throws Exception {
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
    public void getAllCompanyHeadcountInvalid() throws Exception {
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
