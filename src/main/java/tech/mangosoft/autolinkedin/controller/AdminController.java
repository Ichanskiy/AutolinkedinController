package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.service.AccountService;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;

import java.util.List;

import static tech.mangosoft.autolinkedin.controller.LinkedinContactController.COUNT_TO_PAGE;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @CrossOrigin
    @GetMapping("/all/{page}")
    public ResponseEntity<PageImpl<Account>> getAllAccount(@PathVariable Integer page) {
        List<Account> accounts = accountService.getAllAccounts(page);
        if (accounts == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        long count = accountRepository.count();
        return new ResponseEntity<>(new PageImpl<>(accounts,
                PageRequest.of(page, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountRepository.getById(id);
        return account == null
                ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : new ResponseEntity<>(account, HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping
    public ResponseEntity<Account> deleteAccount(@RequestBody String login) {
        Account accountDB = accountRepository.getAccountByUsername(login);
        if (accountDB == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountService.delete(accountDB);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
