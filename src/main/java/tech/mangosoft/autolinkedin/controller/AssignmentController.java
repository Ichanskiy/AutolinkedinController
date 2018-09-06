package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.LinkedInService;
import tech.mangosoft.autolinkedin.controller.messages.*;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.controller.LinkedinContactController.COUNT_TO_PAGE;
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
        Assignment assignment = new Assignment(TASK_GRABBING,
                gm.getLocation(),
                gm.getFullLocationString(),
                gm.getPosition(),
                gm.getIndustries(),
                account);
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
        Assignment assignment = new Assignment(TASK_CONNECTION,
                cm.getLocation(),
                cm.getFullLocationString(),
                cm.getPosition(),
                cm.getIndustries(),
                cm.getMessage(),
                cm.getExecutionLimit(),
                account);
        if (linkedInService.checkMessageAndPosition(assignment)) {
            logger.log(Level.WARNING, "Message or position must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignmentDB = linkedInService.createConnectionAssignment(assignment);
        return new ResponseEntity<>(assignmentDB, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getStatistics")
    public ResponseEntity<PageImpl<StatisticResponse>> getStatistics(String email, Integer page) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Integer count = linkedInService.getCountAssignment(account);
        List<StatisticResponse> statisticResponse = linkedInService.getStatistics(account, page, 20);
        return new ResponseEntity<>(new PageImpl<>(statisticResponse, PageRequest.of(page - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getStatisticsByDays")
    public ResponseEntity<StatisticsByTwoDaysMessage> getStatisticsByTwoDays(String email) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getStatisticsByTwoDays(account), HttpStatus.OK);
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

    @CrossOrigin
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        linkedInService.deleteAssignmentById(assignment.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin
    @PutMapping("/changeStatus")
    public ResponseEntity<HttpStatus> changeAssignmentStatus(Long id, Integer status) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        linkedInService.changeStatus(id, status);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getConnectionInfo/{id}")
    public ResponseEntity<StatisticsByConnectionMessage> getConnectionInfoByAssignmentId(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        StatisticsByConnectionMessage statistics = linkedInService.getContactsByConnection(assignment);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping(value = "/deleteContactsFromAssignment")
    public ResponseEntity<HttpStatus> deleteContactsFromAssignment(Long id, List<Long> contactsIds) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (CollectionUtils.isEmpty(contactsIds)) {
            logger.log(Level.WARNING, "List must be not null or empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        linkedInService.deleteContactsFromAssignment(assignment, contactsIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @CrossOrigin
//    @GetMapping
//    public ResponseEntity<HttpStatus> getTest() {
//        Assignment assignment = assignmentRepository.getById(150L);
//        if (assignment == null) {
//            logger.log(Level.WARNING, "Assignment must be not null");
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
}
