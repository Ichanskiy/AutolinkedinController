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
import tech.mangosoft.autolinkedin.db.repository.ICompanyRepository;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.COMPANY_CONTROLLER;


@RunWith(MockitoJUnitRunner.class)
public class CompanyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICompanyRepository companyRepository;

    @InjectMocks
    private CompanyController companyController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(companyController).dispatchOptions(true).build();
    }

    @Test
    public void getCompaniesValid() throws Exception {
        String request = COMPANY_CONTROLLER;
        when(companyRepository.findAll()).thenReturn(new ArrayList<>());
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        assertNotEquals(response.getContentAsString(), Strings.EMPTY);
        verify(companyRepository, times(1)).findAll();
        verifyNoMoreInteractions(companyRepository);
    }



    @Test
    public void getCompaniesInvalid() throws Exception {
        String request = COMPANY_CONTROLLER;
        when(companyRepository.findAll()).thenReturn(null);
        MockHttpServletResponse response = mockMvc
                .perform(get(request))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        assertEquals(response.getContentAsString(), Strings.EMPTY);
        verify(companyRepository, times(1)).findAll();
        verifyNoMoreInteractions(companyRepository);
    }
}
