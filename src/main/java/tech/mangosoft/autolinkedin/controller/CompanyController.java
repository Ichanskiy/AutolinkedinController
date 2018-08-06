package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.AccountService;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Company;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.ICompanyRepository;

import java.util.List;


@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private ICompanyRepository companyRepository;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<List<Company>> getCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : new ResponseEntity<>(companies, HttpStatus.OK);
    }
}
