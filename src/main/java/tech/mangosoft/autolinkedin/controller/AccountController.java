package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.service.AccountService;

import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;


@RestController
@RequestMapping(ACCOUNT_CONTROLLER)
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private IAccountRepository accountRepository;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Account> login(String login, String password) {
        Account account = accountRepository.getAccountByUsernameAndPasswordAndConfirmIsTrue(login, password);
        return account == null ?
                new ResponseEntity<>(HttpStatus.BAD_REQUEST) : new ResponseEntity<>(account, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Account> registration(@RequestBody Account account) {
        if (accountService.accountNotValid(account)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Account accountDB = accountService.createAccount(account);
        return new ResponseEntity<>(accountDB, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(BY_ID)
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountRepository.getById(id);
        return account == null
                ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : new ResponseEntity<>(account, HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteAccount(@RequestBody String login) {
        Account accountDB = accountRepository.getAccountByUsername(login);
        if (accountDB == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountService.delete(accountDB);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin
    @PutMapping
    public ResponseEntity<Account> updateAccount(Account account) {
        Account accountDB = accountRepository.getById(account.getId());
        if (accountDB == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(accountService.update(account), HttpStatus.OK);
    }

    @CrossOrigin
    @PutMapping(BY_PASSWORD)
    public ResponseEntity<HttpStatus> updatePassword(String username, String oldPassword, String newPassword) {
        if (!accountService.updatePasswordSuccesses(username, oldPassword, newPassword)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
