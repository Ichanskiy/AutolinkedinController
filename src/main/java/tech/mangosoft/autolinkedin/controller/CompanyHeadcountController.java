package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.db.entity.CompanyHeadcount;
import tech.mangosoft.autolinkedin.db.repository.ICompanyHeadcountRepository;

import java.util.Comparator;
import java.util.List;

import static tech.mangosoft.autolinkedin.controller.ControllerAPI.ALL;
import static tech.mangosoft.autolinkedin.controller.ControllerAPI.HEADCOUNT_CONTROLLER;

@RestController
@RequestMapping(HEADCOUNT_CONTROLLER)
public class CompanyHeadcountController {

    @Autowired
    private ICompanyHeadcountRepository headcountRepository;

    @CrossOrigin
    @GetMapping(ALL)
    public ResponseEntity<List<CompanyHeadcount>> getAllCompanyHeadcount() {
        List<CompanyHeadcount> companyHeadcounts = headcountRepository.findAll();
        if(companyHeadcounts == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Comparator<CompanyHeadcount> comparator = (left, right) -> (int) (left.getId() - right.getId());
        companyHeadcounts.sort(comparator);
        return new ResponseEntity<>(companyHeadcounts, HttpStatus.OK);
    }

}
