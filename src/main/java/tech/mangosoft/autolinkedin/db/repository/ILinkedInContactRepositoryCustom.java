package tech.mangosoft.autolinkedin.db.repository;

import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;

import java.util.List;

public interface ILinkedInContactRepositoryCustom {

    public LinkedInContact getNextAvailableContact(int page, Assignment assignment);

    public boolean updateContactStatus(LinkedInContact contactId, Account account, int status, String error, String audit, Long processingReportId);

    public boolean saveNewContactsBatch(List<LinkedInContact> contacts, Long processingReportId);

}
