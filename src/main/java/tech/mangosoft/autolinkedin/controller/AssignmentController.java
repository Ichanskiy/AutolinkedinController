package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.ContactService;
import tech.mangosoft.autolinkedin.LinkedInService;
import tech.mangosoft.autolinkedin.controller.messages.ConnectionMessage;
import tech.mangosoft.autolinkedin.controller.messages.ContactsMessage;
import tech.mangosoft.autolinkedin.controller.messages.GrabbingMessage;
import tech.mangosoft.autolinkedin.controller.messages.StatisticResponse;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.entity.enums.Status;
import tech.mangosoft.autolinkedin.db.entity.enums.Task;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILinkedInContactRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_CONNECTION;
import static tech.mangosoft.autolinkedin.db.entity.enums.Task.TASK_GRABBING;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    private Logger logger = Logger.getLogger(AssignmentController.class.getName());

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private LinkedInService linkedInService;

    @Autowired
    private ILocationRepository locationRepository;

    @CrossOrigin
    @PostMapping(value = "/createGrabbing")
    public ResponseEntity<Assignment> createGrabbingAssignment(GrabbingMessage gm) {
        Account account = accountRepository.getAccountByUsername(gm.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = new Assignment(TASK_GRABBING, gm.getLocation(), gm.getFullLocationString(), gm.getPosition(), gm.getIndustries(), account);
        if (linkedInService.checkAllField(assignment)) {
            logger.log(Level.WARNING, "Fields must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignmentDB = assignmentRepository.save(assignment);
        return new ResponseEntity<>(assignmentDB, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/createConnection")
    public ResponseEntity<Assignment> createConnectionAssignment(ConnectionMessage cm) {
        Account account = accountRepository.getAccountByUsername(cm.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = new Assignment(TASK_CONNECTION, cm.getLocation(), cm.getFullLocationString(), cm.getPosition(), cm.getIndustries(), cm.getMessage(), account);
        if (linkedInService.checkMessageAndPosition(assignment)) {
            logger.log(Level.WARNING, "Message or position must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignmentDB = assignmentRepository.save(assignment);
        return new ResponseEntity<>(assignmentDB, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getStatistics")
    public ResponseEntity<List<StatisticResponse>> getStatistics(String email, Integer page) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<StatisticResponse> statisticResponse = linkedInService.getStatistics(account, page, 20);
        return new ResponseEntity<>(statisticResponse, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getLocations")
    public ResponseEntity<List<String>> getLocations() {
        Iterable<Location> statisticResponse = locationRepository.findAll();
        List<String> stringLocations = new ArrayList<>();
        for (Location location : statisticResponse) {
            stringLocations.add(location.getLocation());
        }
        return new ResponseEntity<>(stringLocations, HttpStatus.OK);
    }

}
