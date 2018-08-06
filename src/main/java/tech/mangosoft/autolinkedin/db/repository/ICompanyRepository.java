package tech.mangosoft.autolinkedin.db.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Company;

import java.util.List;

public interface ICompanyRepository extends CrudRepository<Company, Long> {

    Company getById(Long id);

    Company getByName(String companyName);

    List<Company> findAll();
}
