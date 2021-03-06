package tech.mangosoft.autolinkedin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mangosoft.autolinkedin.db.repository.*;
import tech.mangosoft.autolinkedin.controller.messages.*;
import tech.mangosoft.autolinkedin.db.entity.Account;
import tech.mangosoft.autolinkedin.db.entity.Assignment;
import tech.mangosoft.autolinkedin.db.entity.Location;
import tech.mangosoft.autolinkedin.db.entity.Group;
import tech.mangosoft.autolinkedin.controller.messages.StatisticResponse;
import tech.mangosoft.autolinkedin.service.LinkedInService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tech.mangosoft.autolinkedin.controller.ControllerAPI.*;
import static tech.mangosoft.autolinkedin.controller.LinkedinContactController.COUNT_TO_PAGE;

@RestController
@RequestMapping(ASSIGNMENT_CONTROLLER)
public class AssignmentController {

    private Logger logger = Logger.getLogger(AssignmentController.class.getName());

    @Autowired
    private IAssignmentRepository assignmentRepository;

    @Autowired
    private IGroupRepository groupRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private LinkedInService linkedInService;

    @Autowired
    private ILocationRepository locationRepository;

    @CrossOrigin
    @PostMapping(value = CREATE_GRABBING)
    public ResponseEntity<Assignment> createGrabbingAssignment(@RequestBody GrabbingMessage message) {
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
    @PostMapping(value = CREATE_GRABBING_SALES)
    public ResponseEntity<Assignment> createGrabbingSalesAssignment(@RequestBody GrabbingMessage message) {
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
    @PostMapping(value = CREATE_CONNECTION)
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
    @GetMapping(BY_ID)
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.getById(id);
        if (assignment == null) {
            logger.log(Level.WARNING, "Assignment must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(assignment, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = GET_STATISTICS)
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
    @GetMapping(value = GET_GROUPS)
    public List<Group> getGroups() {
        return groupRepository.findAll();
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
    @GetMapping(value = GET_LOCATIONS)
    public ResponseEntity<List<String>> getLocations() {
        Iterable<Location> statisticResponse = locationRepository.findAll();
        List<String> stringLocations = new ArrayList<>();
        for (Location location : statisticResponse) {
            stringLocations.add(location.getLocation());
        }
        return new ResponseEntity<>(stringLocations, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = GET_INDUSTRIES)
    public ResponseEntity<List<String>> getIndustries() {
        return new ResponseEntity<>(assignmentRepository.getAllIndustries(), HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping(BY_ID)
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
    @PutMapping(CHANGE_STATUS)
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
    @GetMapping(value = GET_CONNECTION_INFO_BY_ID_AND_PAGE)
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
    @GetMapping(value = GET_ASSIGNMENT_BY_USER_AND_STATUS)
    public ResponseEntity<PageImpl<Assignment>> getAssignmentByUserAndStatus(String email, Integer status, Integer count) {
        Account account = accountRepository.getAccountByUsername(email);
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getAssignmentByUserAndStatus(account, status, count), HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = GET_ASSIGNMENT_BY_PARAM)
    public ResponseEntity<PageImpl<Assignment>> getAssignmentsByParam(@RequestBody AssignmentsByParam message) {
        Account account = accountRepository.getAccountByUsername(message.getEmail());
        if (account == null) {
            logger.log(Level.WARNING, "Account must be not null");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(linkedInService.getAssignmentByParam(message, account), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = GET_GRAPH_BY_TYPE)
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
