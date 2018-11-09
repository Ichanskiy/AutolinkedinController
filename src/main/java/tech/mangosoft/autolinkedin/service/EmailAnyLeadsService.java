package tech.mangosoft.autolinkedin.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.mangosoft.autolinkedin.db.entity.LinkedInContact;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Service
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class EmailAnyLeadsService {

    private static final Logger logger = Logger.getLogger(EmailAnyLeadsService.class.getName());
    private static final String KEY_VALUE = "8068597d2a925704743974f6b587e990ac";
    private static final String URL = "https://api.anyleads.com/v1/permutation/?first_name=FN&last_name=LS&domain=DOM&api_key=KEY";
    private static final String FIRST_NAME = "FN";
    private static final String LAST_NAME = "LS";
    private static final String DOMAIN = "DOM";
    private static final String API_KEY = "KEY";

    @Autowired
    private ILinkedInContactRepository contactRepository;

    @Scheduled(cron = "0/30 * * * * ?")
    public void getEmailFromAnnyLeads() {
        LinkedInContact linkedInContact = contactRepository
                .getFirstByEmailIsNullAndCompanyWebsiteNotNullAndGrabbedEmailIsNullOrderByIdDesc();
        if (linkedInContact == null) {
            return;
        }
        if (linkedInContact.getCompanyWebsite() == null) {
            contactRepository.save(linkedInContact.setGrabbedEmail(false));
        }
        HttpUriRequest request = new HttpGet(URL
                .replace(FIRST_NAME, linkedInContact.getFirstName())
                .replace(LAST_NAME, linkedInContact.getLastName())
                .replace(DOMAIN, getDomain(linkedInContact.getCompanyWebsite()))
                .replace(API_KEY, KEY_VALUE));
        logger.info("Id = " + linkedInContact.getId() + " " + request.toString());
        HttpResponse response = null;
        HttpEntity entity = null;
        try {
            response = HttpClientBuilder.create().build().execute(request);
            entity = response.getEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonString = null;
        try {
            assert entity != null;
            jsonString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject(jsonString);
        Object emailObj = null;
        try {
            emailObj = json.get("email");
        } catch (JSONException e) {
            contactRepository.save(linkedInContact.setGrabbedEmail(false));
            logger.info("Email not fount id = " + linkedInContact.getId());
            return;
        }
        String email = emailObj.toString();
        if (email.contains("@")) {
            setEmailAndSaveContact(linkedInContact, email);
        } else {
            contactRepository.save(linkedInContact.setGrabbedEmail(false));
        }
        logger.info("\n" + jsonString);

    }

    @Transactional
    void setEmailAndSaveContact(LinkedInContact contact, String email){
        LinkedInContact linkedInContact = contactRepository.getById(contact.getId());
        contactRepository.save(linkedInContact.setEmail(email));
    }

    private String getDomain(String companyWebsite) {
        return companyWebsite.replace("http://www.", "").replace("/", "");
    }

}
