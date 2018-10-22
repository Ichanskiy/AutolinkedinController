package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.db.entity.CompanyHeadcount;
import tech.mangosoft.autolinkedin.db.repository.ICompanyHeadcountRepository;

import java.util.List;

@RestController
@RequestMapping("/headcount")
public class CompanyHeadcountController {

    @Autowired
    private ICompanyHeadcountRepository headcountRepository;

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity<List<CompanyHeadcount>> getAllCompanyHeadcount() {
        List<CompanyHeadcount> companyHeadcounts = headcountRepository.findAll();
        return new ResponseEntity<>(companyHeadcounts, HttpStatus.OK);
    }
}
