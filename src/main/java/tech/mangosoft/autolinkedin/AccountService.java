package tech.mangosoft.autolinkedin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.entity.enums.Role;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.ICompanyRepository;

import java.util.List;
import java.util.logging.Logger;

/**
 * <h1> LinkedIn Service!</h1>
 * The LinkedInService implements initial logic application
 * <p>
 *
 * Method annotate @Scheduled is point to start do assignment
 * user friendly and it is assumed as a high quality code.
 *
 *
 * @author  Ichanskiy
 * @version 1.0
 * @since   2018-06-06
 */
@Service
public class AccountService {

    private static Logger logger = Logger.getLogger(AccountService.class.getName());

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private ICompanyRepository companyRepository;

    /**
     * @author  Ichanskiy
     *
     * This is the method check valid account or not.
     * @param account input object with field.
     * @return if valid then true, else false
     */
    public boolean accountNotValid(Account account) {
        return account.getGrabbingLimit() == null
                || account.getPassword() == null
                || account.getUsername() == null;
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method update account`s field.
     * @param account input object
     * @return updated object
     */
    public Account update(Account account) {
        Account accountDb = accountRepository.getById(account.getId());
        accountDb.setGrabbingLimit(account.getGrabbingLimit());
        if (account.getCompany() != null && account.getCompany().getName() != null){
            String companyName = account.getCompany().getName();
            Company company = companyRepository.getByName(companyName);
            accountDb.setCompany(company != null ? company : companyRepository.save(company.setName(companyName)));
        }
        accountDb.setUsername(account.getUsername());
        accountDb.setRole(account.getRole());
        accountDb.setFirst(account.getFirst());
        accountDb.setLast(account.getLast());
        return accountRepository.save(accountDb);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method update account`s password.
     * @param username input username
     * @param oldPassword old password
     * @param newPassword new password
     * @return if project updated then return true, else false
     */
    public boolean updatePasswordSuccesses(String username, String oldPassword, String newPassword) {
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        if (!passwordEquals(account, oldPassword)) {
            return false;
        }
        account.setPassword(newPassword);
        accountRepository.save(account);
        return true;
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method chesk equlas password.
     * @param account input account
     * @param oldPassword old password
     * @return if password equals password then return true, else false
     */
    private boolean passwordEquals(Account account, String oldPassword) {
        return account.getPassword().equals(oldPassword);
    }


    /**
     * @author  Ichanskiy
     *
     * This is the method save account.
     * @param account input account
     * @return saved account
     */
    public Account createAccount(Account account) {
        Company company = companyRepository.getByName(account.getCompany().getName());
        account.setCompany(company);
        account.setRole(Role.USER);
        return accountRepository.save(account);
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method delete account.
     * @param account input account
     */
    public void delete(Account account) {
        account.setContactProcessings(null);
        accountRepository.delete(accountRepository.save(account));
    }

    /**
     * @author  Ichanskiy
     *
     * This is the method delete account.
     * @param account input account
     */
    public List<Account> getAllAccounts(Integer page) {
        return accountRepository.findAll(PageRequest.of(page - 1, 40,  Sort.Direction.DESC, "id")).getContent();
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}
