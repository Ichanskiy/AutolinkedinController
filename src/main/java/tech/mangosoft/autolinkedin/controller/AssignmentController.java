package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.service.LinkedInService;
import tech.mangosoft.autolinkedin.controller.messages.*;
import tech.mangosoft.autolinkedin.db.entity.*;
import tech.mangosoft.autolinkedin.db.repository.IAccountRepository;
import tech.mangosoft.autolinkedin.db.repository.IAssignmentRepository;
import tech.mangosoft.autolinkedin.db.repository.ILocationRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.controller.LinkedinContactController.COUNT_TO_PAGE;

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
    public ResponseEntity<Assignment> createGrabbingAssignment(GrabbingMessage message) {
        Account account = accountRepository.getAccountByUsername(message.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = linkedInService.createGrabbingAssignment(message, account);
        if (assignment == null) {
            logger.log(Level.WARNING, "Fields must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(assignment, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/createGrabbingSales")
    public ResponseEntity<Assignment> createGrabbingSalesAssignment(GrabbingMessage message) {
        Account account = accountRepository.getAccountByUsername(message.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = linkedInService.createGrabbingSalesAssignment(message, account);
        if (assignment == null) {
            logger.log(Level.WARNING, "Message or position must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(assignment, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/createConnection")
    public ResponseEntity<Assignment> createConnectionAssignment(ConnectionMessage message) {
        Account account = accountRepository.getAccountByUsername(message.getLogin());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Assignment assignment = linkedInService.createConnectionAssignment(message, account);
        if (assignment == null) {
            logger.log(Level.WARNING, "Message or position must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(assignment, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(assignment, HttpStatus.OK);
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
        return new ResponseEntity<>(new PageImpl<>(statisticResponse,
                PageRequest.of(page - 1, COUNT_TO_PAGE), count), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "/getStatisticsByDays")
    public ResponseEntity<StatisticsByDaysMessage> getStatisticsByDays(String email, Date from, Date to) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getStatisticsByDays(account, from, to), HttpStatus.OK);
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
    @GetMapping(value = "/getConnectionInfo/{id}/{page}")
    public ResponseEntity<StatisticsByConnectionMessage> getConnectionInfoByAssignmentId(@PathVariable Long id,
                                                                                         @PathVariable Integer page) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        StatisticsByConnectionMessage statistics = linkedInService.getContactsByConnection(assignment, page);
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

    @CrossOrigin
    @GetMapping(value = "/getAssignmentByUserAndStatus")
    public ResponseEntity<PageImpl<Assignment>> getAssignmentByUserAndStatus(String email, Integer status, Integer count) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getAssignmentByUserAndStatus(account, status, count), HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/getAssignmentByParam")
    public ResponseEntity<PageImpl<Assignment>> getAssignmentsByParam(@RequestBody AssignmentsByParam message) {
        Account account = accountRepository.getAccountByUsername(message.getEmail());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getAssignmentByParam(message, account), HttpStatus.OK);
    }

 /*   @CrossOrigin
    @GetMapping(value = "/getGraph")
    public ResponseEntity<GraphMessage> getGraphByAccount(String email) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getGraphByAccount(account), HttpStatus.OK);
    }
*/
    @CrossOrigin
    @GetMapping(value = "/getGraphByType")
    public ResponseEntity<GraphMessage> getGraphByType(String email, String type, String period) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        int periodLength = 7;
        if (period != null && period.equals("monthly")) {
            periodLength = 30;
        }

        if (type.equals("links") || type.equals("messages") || type.equals("errors") ) {
            return new ResponseEntity<>(linkedInService.getGraphByType(account, type, periodLength), HttpStatus.OK);

        }

        logger.log(Level.WARNING, "graph type " + type + " is unsupported");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
